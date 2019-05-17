# Copyright 2019 EMBL - European Bioinformatics Institute
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import concurrent.futures
import sys
import time

import urllib
import urllib.error
import urllib.request



def load_url(url, timeout):
    try:
        with urllib.request.urlopen(url, timeout=timeout) as response:
            return 'OK', response.code
    except urllib.error.HTTPError as e:
        return e.reason, e.code


def parallel_web_service_requests(url, num_threads, ensure_success=False):
    with concurrent.futures.ThreadPoolExecutor(max_workers=num_threads) as executor:
        future_to_url = {executor.submit(load_url, url, 60): url for _ in range(0, num_threads)}
        for future in concurrent.futures.as_completed(future_to_url):
            current_url = future_to_url[future]
            try:
                result = future.result()
                print(result)
                if ensure_success and (result[1] != 200):
                    print("Request failed when it shouldn't have!")
                    sys.exit(1)
            except Exception as exc:
                print('%r generated an exception: %s' % (current_url, exc))


def success_use_case(url):
    for i in range(0, 20):
        parallel_web_service_requests(url, 10, True)
        time.sleep(5)


def failure_use_case(url):
    parallel_web_service_requests(url, 100)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 test_web_service_rate_limiting.py <WEB_SERVICE_HOST_URL> "
              "(ex: python3 test_web_service_rate_limiting.py http://www.ebi.ac.uk) [nosleep]")
        sys.exit(1)
    urlString = "{0}/eva/webservices/rest/v1/segments/1:105000001-105500000/variants?species=mmusculus_grcm38&limit=5"\
        .format(sys.argv[1])
    print("To test parallel requests from multiple IP addresses, "
          "please run this script with the nosleep argument within 1 minute from other machines...")

    if len(sys.argv) == 2:
        time.sleep(60)  # Allow some time for the script to be invoked in multiple machines

    print("****************************************************")
    print("All the service requests below should be successful!")
    success_use_case(urlString)
    print("*****************************************************")

    time.sleep(30)

    print("**********************************************************************")
    print("Some of the following service requests below should NOT be successful!")
    print("**********************************************************************")
    failure_use_case(urlString)
