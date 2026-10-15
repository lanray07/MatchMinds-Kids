"""Measure process-cold launches of the installed release without changing its play data."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import statistics
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PACKAGE = 'com.matchminds.kids'


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--adb', required=True)
    parser.add_argument('--runs', type=int, default=5)
    args = parser.parse_args()
    if not 1 <= args.runs <= 10:
        raise ValueError('Use 1–10 launches')

    def adb(*parts):
        return subprocess.run([args.adb, *parts], check=True, text=True, capture_output=True, timeout=30).stdout

    if len([line for line in adb('devices').splitlines() if line.endswith('\tdevice')]) != 1:
        raise RuntimeError('Exactly one authorized test device is required')
    model = adb('shell', 'getprop', 'ro.product.model').strip()
    api = int(adb('shell', 'getprop', 'ro.build.version.sdk').strip())
    launches = []
    for _ in range(args.runs):
        adb('shell', 'am', 'force-stop', PACKAGE)
        output = adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/.MainActivity')
        if 'Status: ok' not in output:
            raise RuntimeError('Release activity did not start')
        launches.append({key: int(re.search(r'^' + key + r':\s*(\d+)', output, re.M).group(1)) for key in ['TotalTime', 'WaitTime']})
    resumed = adb('shell', 'dumpsys', 'activity', 'activities')
    if not any(PACKAGE + '/.MainActivity' in line for line in resumed.splitlines() if 'mResumedActivity' in line):
        raise RuntimeError('Release is not the foreground activity')
    memory = adb('shell', 'dumpsys', 'meminfo', PACKAGE)
    pss = re.search(r'^\s*TOTAL\s+(\d+)', memory, re.M)
    report = {'device_model': model, 'android_api': api, 'package': PACKAGE, 'passed': True,
              'launches': launches, 'median_process_cold_total_time_ms': statistics.median(x['TotalTime'] for x in launches),
              'last_launch_pss_kb': int(pss.group(1)) if pss else None,
              'scope': 'Five force-stopped process launches; not an OS-cold boot or gameplay frame-rate measurement.',
              'apk_sha256': hashlib.sha256((ROOT/'app/build/outputs/apk/release/app-release.apk').read_bytes()).hexdigest()}
    (ROOT/'artifacts/fire-release-performance.json').write_text(json.dumps(report, indent=2)+'\n', encoding='utf-8')
    print(json.dumps(report, indent=2))


if __name__ == '__main__':
    main()
