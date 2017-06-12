import os
import subprocess
import time
import sys
import logging
import traceback

processes = []

try:
    orig_dir = os.getcwd()

    top_dir = os.path.join(os.path.dirname(__file__), "..")

    os.chdir(top_dir)
    top_dir = os.getcwd()

    # Launch core
    os.chdir(os.path.join(top_dir, "core"))
    core_proc = subprocess.Popen(['java', "-jar", os.path.join("target", "core-1.0.0-beta.jar")])
    processes.append(core_proc)

    # Launch PSS
    os.chdir(os.path.join(top_dir, "pss"))
    pss_proc = subprocess.Popen(['java', "-jar", os.path.join("target", "pss-1.0.0-beta.jar")])
    processes.append(pss_proc)

    os.chdir(orig_dir)
    while 1:
        time.sleep(1)

except Exception as e:
    logging.error(traceback.format_exc())
    for proc in processes:
        proc.kill()
    sys.exit()

