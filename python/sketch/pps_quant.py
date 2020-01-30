import numpy as np


class PPSQuantSketch:
    def __init__(self, size, seed=0):
        self.size = size
        self.random = np.random.RandomState(0)
        self.item_counts = []
        self.ncounts = 0

    def update(self, new_item_counts):
        self.ncounts += sum([v[1] for v in new_item_counts])
        self.item_counts += new_item_counts
        if len(self.item_counts) > self.size:
            self.compress()

    def compress(self):
        compressed_values = dict()

        nsegs = int(self.size*.7)
        skip_offset = self.ncounts / nsegs
        target_offsets = self.random.uniform(0, skip_offset, size=nsegs+1)
        target_offsets += np.arange(nsegs+1)*skip_offset
        # target_offsets = target_offsets[0] + np.arange(nsegs+1)*skip_offset

        seg_idx = 0
        cur_offset = 0
        self.item_counts.sort(key=lambda x: x[0])
        for k,v in self.item_counts:
            cur_offset += v
            while cur_offset > target_offsets[seg_idx]:
                compressed_values[k] = compressed_values.get(k, 0) + skip_offset
                seg_idx += 1

        self.item_counts = sorted([(k,v) for k,v in compressed_values.items()], key=lambda x: x[0])

    def get_dict(self):
        ret_vals = dict()
        for k,v in self.item_counts:
            ret_vals[k] = ret_vals.get(k,0) + v
        return ret_vals
