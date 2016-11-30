import json
import sys

infile = open(sys.argv[1], "rt")
outfile = open(sys.argv[2], "wt")

outfile.write("{\n\"variants_1_2\":[\n")

for line in infile:
    json_dict = json.loads(line.rstrip())
    for idx in range(len(json_dict["files"])):
        del json_dict["files"][idx]["attrs"]["src"]
    outfile.write(json.dumps(json_dict) + ",\n")

outfile.write("]\n}")

outfile.close()
infile.close()

