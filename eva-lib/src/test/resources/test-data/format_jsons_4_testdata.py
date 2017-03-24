import json
import os
import io
import sys

# Script used to convert a file with one JSON per line, to a file suitable for Lord of the Jars 
# to use as a test dataset to load to a testing Mongo database.
# Lord of the Jars requires a json file in the format:
# {collection_name:[...documents for entry into collection...]}
#
# JSONs cannot be loaded if they have the src field, since the Java MongoDB driver cannot 
# currently handle the $type value of $binary specified as a hexadecimal string (the format output
# by mongoexport). Due to this, the src object is removed. The bug in the driver has been fixed and
# will be available in a future driver version.

with open(sys.argv[1], "rt") as infile:
    with open(sys.argv[2], "wt") as outfile:

        outfile.write("{\n\"variants_1_2\":[\n")

        for line in infile:
            json_dict = json.loads(line.rstrip())
            for idx in range(len(json_dict["files"])):
                if "src" in json_dict["files"][idx]["attrs"]:
                    del json_dict["files"][idx]["attrs"]["src"]
            outfile.write(json.dumps(json_dict) + ",\n")

        outfile.write("]\n}")
