import random

time = ["0"] * 5 + ["1"] * 5 + ["2"] * 5 + ["3"] * 4 + ["4"] * 3 + ["5"] * 2 + ["6", "7", "8"]
for i in range(30):
    print(random.choice(time))
