class MisraGries:
    def __init__(self, size):
        self.size = size
        self.items = dict()
        self.floor = 0

    def update(self, val, weight=1):
        if val in self.items:
            self.items[val] += weight
        else:
            self.items[val] = weight

        if len(self.items) > self.size:
            self.compress(int(.7*self.size))

    def compress(self, target_size):
        if len(self.items) <= target_size:
            return
        weights = sorted(self.items.values())[::-1]
        cutoff_weight = weights[target_size]
        self.items = {
            v:w-cutoff_weight for v,w in self.items.items()
            if w > cutoff_weight
        }
        self.floor += cutoff_weight

    def get_dict(self):
        return {
            v: w+self.floor for v,w in self.items.items()
        }
