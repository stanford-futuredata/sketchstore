import math
from typing import List, Mapping

import numpy as np
import pandas as pd
from tqdm import tqdm

import sketch.compress_freq as cf
import sketch.sketch_frequent as f
import sketch.compress_dyadic as dyadic
import sketch.sketch_gen as board_sketch


class BoardGen:
    def __init__(self, sketch_gen: board_sketch.SketchGen):
        self.sketch_gen = sketch_gen

    def generate(
            self,
            segments: List,
            tags: List[Mapping],
    ) -> pd.DataFrame:
        n_segments = len(segments)
        result_rows = []
        for seg_idx in tqdm(range(n_segments)):
            cur_segment = segments[seg_idx]
            cur_tags = tags[seg_idx]
            sketch_combos = self.sketch_gen.generate(
                cur_segment,
                cur_tags
            )
            for bsketch, additional_tags in sketch_combos:
                new_row = cur_tags.copy()
                new_row.update(additional_tags)
                new_row["sketch"] = self.sketch_gen.name()
                new_row["data"] = bsketch
                new_row["seg_idx"] = seg_idx
                result_rows.append(
                    new_row
                )
        return pd.DataFrame(result_rows)

    @classmethod
    def serialize(cls, df: pd.DataFrame, fname: str):
        df.to_pickle(fname)

    @classmethod
    def deserialize(cls, fname: str):
        pd.read_pickle(fname)

