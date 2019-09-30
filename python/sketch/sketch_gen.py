import json
from typing import Dict, List, Tuple, Mapping
import pickle
import sketch.sketch_frequent as frequent
import sketch.compress_freq as compress_freq
from sketch.compressor import ItemDictCompressor,SeqDictCompressor
from sketch.compress_dyadic import DyadicFrequencyCompressor, DyadicQuantileCompressor
import bounter
import numpy as np


class BoardSketch:
    def name(self) -> str:
        raise NotImplemented

    def estimate(self, x, rank: bool = False) -> float:
        raise NotImplemented


class DictSketch(BoardSketch):
    def __init__(self, vals: Dict):
        self.vals = vals

    def name(self) -> str:
        return "dict"

    def estimate(self, x, rank=False) -> float:
        if rank:
            return sum([
                v for k, v in self.vals
                if k <= x
            ])
        else:
            return self.vals.get(x, 0)


class CMSSketch(BoardSketch):
    def __init__(self, cms_obj: bounter.count_min_sketch.CountMinSketch):
        self.cms_obj = cms_obj

    def name(self) -> str:
        return "countmin"

    def estimate(self, x, rank:bool=False) -> float:
        return self.cms_obj[str(x)]


class SketchGen:
    def generate(self, xs, args: Mapping) -> List[Tuple[BoardSketch, Dict]]:
        raise NotImplemented

    def name(self) -> str:
        return "BaseSketchGen"


class SpaceSavingGen(SketchGen):
    def __init__(self):
        pass

    def name(self) -> str:
        return "spacesave"

    def generate(self, xs, args: Mapping) -> List[Tuple[BoardSketch, Dict]]:
        size = args["size"]
        ss = frequent.SpaceSavingSketch(size=size, unbiased=False)
        ss.add(xs)
        return [(DictSketch(ss.get_dict()), dict())]


class CMSGen(SketchGen):
    def name(self) -> str:
        return "countmin"

    def generate(self, xs, args: Mapping) -> List[Tuple[BoardSketch, Dict]]:
        size = args["size"]
        cms = bounter.count_min_sketch.CountMinSketch(width=size, depth=5)
        cms.update([str(x) for x in xs])
        return [(CMSSketch(cms_obj=cms), dict())]


class ItemDictCompressorGen(SketchGen):
    def __init__(self, name: str, compressor: ItemDictCompressor):
        self._name = name
        self.compressor = compressor

    def name(self) -> str:
        return self._name

    def generate(self, xs, args: Mapping) -> List[Tuple[BoardSketch, Dict]]:
        size = args["size"]
        items, item_counts = np.unique(xs, return_counts=True)
        item_dict = dict(zip(items, item_counts))

        if "bias" in args:
            item_dict = compress_freq.apply_bias(item_dict)
        output_map = self.compressor.compress(item_dict, size=size)
        return [(DictSketch(output_map), dict())]


class DyadicItemDictGen(SketchGen):
    def __init__(self, h_compressor: DyadicFrequencyCompressor):
        self.h_compressor = h_compressor

    def name(self) -> str:
        return "dyadic:2"

    def generate(self, xs, args: Mapping):
        size = args["size"]
        items, item_counts = np.unique(xs, return_counts=True)
        item_dict = dict(zip(items, item_counts))
        output_maps = self.h_compressor.compress(item_dict, size=size)
        return [
            (DictSketch(omap), {"level": i})
            for i,omap in enumerate(output_maps)
        ]