from typing import Dict, Any


class QuantileResultWrapper:
    def __init__(self, x_counts=dict()):
        self.items = x_counts

    def update(self, counts: Dict[Any, float]):
        for k, v in counts.items():
            self.items[k] = self.items.get(k, 0.0) + v

    def rank(self, x):
        return sum((v for k,v in self.items.items() if k <= x))