#include "LBFGSB.h"
#include <Eigen/Core>
#include <vector>
#include <memory>
#include <sstream>
#include <iostream>
#include <fstream>
#include <chrono>

using Eigen::VectorXd;
using Eigen::MatrixXd;
using namespace LBFGSpp;

typedef double Scalar;
typedef Eigen::Matrix<Scalar, Eigen::Dynamic, 1> Vector;

class Timer
{
public:
    void start()
    {
        m_StartTime = std::chrono::system_clock::now();
        m_bRunning = true;
    }

    void stop()
    {
        m_EndTime = std::chrono::system_clock::now();
        m_bRunning = false;
    }

    double elapsedMilliseconds()
    {
        std::chrono::time_point<std::chrono::system_clock> endTime;

        if(m_bRunning)
        {
            endTime = std::chrono::system_clock::now();
        }
        else
        {
            endTime = m_EndTime;
        }

        return std::chrono::duration_cast<std::chrono::milliseconds>(endTime - m_StartTime).count();
    }

    double elapsedSeconds()
    {
        return elapsedMilliseconds() / 1000.0;
    }

private:
    std::chrono::time_point<std::chrono::system_clock> m_StartTime;
    std::chrono::time_point<std::chrono::system_clock> m_EndTime;
    bool                                               m_bRunning = false;
};

class SegmentCCDF {
private:
    std::vector<long> occCounts;
    std::vector<long> occCountFrequency;
public:
    SegmentCCDF(
            std::vector<long>&& occCounts_,
            std::vector<long>&& occCountFrequency_
            ) {
        occCounts = std::move(occCounts_);
        occCountFrequency = std::move(occCountFrequency_);
    }
    void compute(double bias, double* totalResult, double* gradientResult) const {
        size_t n = occCounts.size();
        double total = 0;
        double totalDeriv = 0;
        for (size_t i = 0; i < n; i++) {
            if (occCounts[i] > bias) {
                total += (occCounts[i] - bias) * occCountFrequency[i];
                totalDeriv -= occCountFrequency[i];
            }
        }
        *totalResult = total;
        *gradientResult = totalDeriv;
    }
    void print() const {
        for (long x : occCounts) {
            std::cout << x << " ";
        }
        std::cout << std::endl;
        for (long x : occCountFrequency) {
            std::cout << x << " ";
        }
        std::cout << std::endl;
    }
};

class RMSErrorFunction
{
private:
    std::vector<SegmentCCDF> segments;
    std::vector<int> segmentSpaces;
public:
    RMSErrorFunction(
            std::vector<SegmentCCDF>&& segments_,
            std::vector<int>&& segmentSpaces_
            ) {
        segments = std::move(segments_);
        segmentSpaces = std::move(segmentSpaces_);
    }

    Scalar operator()(const Vector& xBuffer, Vector& grad)
    {
        int nSeg = segments.size();
        double biasTerm = 0;
        double varTerm = 0;

        double ni, dni;
        for (int i = 0; i < nSeg; i++) {
            double x = xBuffer[i];
            const SegmentCCDF& segCDF = segments[i];
//            std::cout << "cdf: " << i << std::endl;
//            segCDF.print();
            segCDF.compute(x, &ni, &dni);

            biasTerm += x;
            double scaledTotal = ni / segmentSpaces[i];
            varTerm += .25*scaledTotal*scaledTotal;
            grad[i] = .5*scaledTotal*dni/segmentSpaces[i];
        }

        for (int i = 0; i < nSeg; i++) {
            grad[i] += 2*biasTerm;
        }
        return biasTerm*biasTerm + varTerm;
    }

    int dim() {
        return segmentSpaces.size();
    }
};

RMSErrorFunction testFunction(int n) {
    std::vector<long> occCounts {1, 2, 3, 5};
    std::vector<long> occCountFreqs {50, 30, 10, 10};

    SegmentCCDF ccdf (
            std::move(occCounts),
            std::move(occCountFreqs)
            );

    std::vector<SegmentCCDF> segments;
    std::vector<int> segmentSpaces;
    for (int i = 0; i < n; i++) {
        segments.push_back(ccdf);
        segmentSpaces.push_back(5);
    }
    return RMSErrorFunction(
            std::move(segments),
            std::move(segmentSpaces)
            );
}

RMSErrorFunction parseFile(const std::string& filePath) {
    std::vector<int> segmentSpaces;
    std::vector<SegmentCCDF> segments;

    std::ifstream infile;
    infile.open(filePath.c_str());

    std::string line;
    std::istringstream iss;
    long number;
    int numInt;

    std::getline(infile, line);
    iss = std::istringstream(line);
    while (iss >> numInt) {
        segmentSpaces.push_back(numInt);
    }

    int numSegments = segmentSpaces.size();

    for (int i = 0; i < numSegments; i++) {
        std::getline(infile, line);
        iss = std::istringstream(line);
        std::vector<long> occCounts;
        occCounts.reserve(line.size());
        while (iss >> number) {
            occCounts.push_back(number);
        }

        std::getline(infile, line);
        iss = std::istringstream(line);
        std::vector<long> occCountFrequency;
        occCountFrequency.reserve(occCounts.size());
        while (iss >> number) {
            occCountFrequency.push_back(number);
        }

        segments.emplace_back(
                std::move(occCounts),
                std::move(occCountFrequency)
                );
    }

    return RMSErrorFunction(
            std::move(segments),
            std::move(segmentSpaces)
            );
}

int main(int argc, char *argv[])
{
    LBFGSBParam<double> param;
    param.max_iterations = 30;
    LBFGSBSolver<double> solver(param);

//    const int n = 10;
//    RMSErrorFunction fun = testFunction(n);
    Timer parseTimer;
    parseTimer.start();
    RMSErrorFunction fun = parseFile(argv[1]);
    parseTimer.stop();
    std::cerr << "Parse Time: " << parseTimer.elapsedMilliseconds() << std::endl;
    int n = fun.dim();

    Vector lb = Vector::Constant(n, 0.0);
    Vector ub = Vector::Constant(n, std::numeric_limits<Scalar>::infinity());
    VectorXd x = VectorXd::Zero(n);
    double fx = 0;

    VectorXd g = VectorXd::Zero(n);
    int niter = solver.minimize(fun, x, fx, lb, ub);

//    std::cout << niter << " iterations" << std::endl;
//    std::cout << "x = \n" << x.transpose() << std::endl;
//    std::cout << "f(x) = " << fx << std::endl;
    std::cout << x.transpose() << std::endl;
    return 0;
}

