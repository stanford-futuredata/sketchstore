from typing import Dict, Any, Mapping, Iterable
import pandas as pd
import numpy as np


class ItemDictCompressor:
    def compress(self, item_dict: Dict[Any, float], size: int) -> Dict[Any, float]:
        raise NotImplemented


class SeqDictCompressor:
    def compress(self, xs: np.ndarray, size: int) -> Dict[Any, float]:
        raise NotImplemented


class DictStreamSketch:
    def add(self, xs: Iterable):
        raise NotImplemented

    def get_dict(self) -> Dict[Any, float]:
        raise NotImplemented


class QuantileResultWrapper:
    def __init__(self, x_counts=None):
        if x_counts is None:
            self.items = dict()
        else:
            self.items = x_counts

    def update(self, counts: Dict[Any, float]):
        for k, v in counts.items():
            self.items[k] = self.items.get(k, 0.0) + v

    def rank(self, x):
        return sum((v for k,v in self.items.items() if k <= x))