import numpy as np
import pandas as pd
from scipy.signal import butter, filtfilt

def read_acc(path):
    # Remove 'app://' if present in the path
    asset_path = path.replace('app://', '')

    # Read CSV content from assets using correct context
    from com.chaquo.python import Python
    from java.io import BufferedReader, InputStreamReader

    context = Python.getPlatform().getApplication()
    input_stream = context.getAssets().open(asset_path)
    reader = BufferedReader(InputStreamReader(input_stream))

    content = ""
    try:
        line = reader.readLine()
        while line is not None:
            content += line + "\n"
            line = reader.readLine()
    finally:
        reader.close()
        input_stream.close()

    # Use StringIO to create file-like object for pandas
    from io import StringIO
    df = pd.read_csv(StringIO(content))
    df[['x-axis (g)', 'y-axis (g)', 'z-axis (g)']] *= 9.81
    return df

def read_gyro(path):
    # Remove 'app://' if present in the path
    asset_path = path.replace('app://', '')

    # Read CSV content from assets using correct context
    from com.chaquo.python import Python
    from java.io import BufferedReader, InputStreamReader

    context = Python.getPlatform().getApplication()
    input_stream = context.getAssets().open(asset_path)
    reader = BufferedReader(InputStreamReader(input_stream))

    content = ""
    try:
        line = reader.readLine()
        while line is not None:
            content += line + "\n"
            line = reader.readLine()
    finally:
        reader.close()
        input_stream.close()

    # Use StringIO to create file-like object for pandas
    from io import StringIO
    df = pd.read_csv(StringIO(content))
    df[['x-axis (deg/s)', 'y-axis (deg/s)', 'z-axis (deg/s)']] *= (np.pi / 180)
    return df

"""## Right Tilt"""
csv_file_acc_path_right_tilt = 'app://Right tilt/Accelerometer.csv'
csv_file_gyro_path_right_tilt = 'app://Right tilt/Gyroscope.csv'
df_acc_right_tilt = read_acc(csv_file_acc_path_right_tilt)
df_gyro_right_tilt = read_gyro(csv_file_gyro_path_right_tilt)

"""## Test Left Right """
csv_file_acc_path_test_left_right_tilt = 'app://Left Right Tilt Test/Accelerometer.csv'
csv_file_gyro_path_test_left_right_tilt = 'app://Left Right Tilt Test/Gyroscope.csv'
df_acc_test_left_right_tilt = read_acc(csv_file_acc_path_test_left_right_tilt)
df_gyro_test_left_right_tilt = read_gyro(csv_file_gyro_path_test_left_right_tilt)

"""## Standing + Sleeping | Right Tilt"""
csv_file_acc_path_standing_sleeping_right_tilt = 'app://Right tilt Standing + Sleeping/Accelerometer.csv'
csv_file_gyro_path_standing_sleeping_right_tilt = 'app://Right tilt Standing + Sleeping/Gyroscope.csv'
df_acc_standing_sleeping_right_tilt = read_acc(csv_file_acc_path_standing_sleeping_right_tilt)
df_gyro_standing_sleeping_right_tilt = read_gyro(csv_file_gyro_path_standing_sleeping_right_tilt)

"""## Sleeping Right Tilt"""
csv_file_acc_path_sleeping_right_tilt = 'app://Sleeping Right Tilt/Accelerometer.csv'
csv_file_gyro_path_sleeping_right_tilt = 'app://Sleeping Right Tilt/Gyroscope.csv'
df_acc_sleeping_right_tilt = read_acc(csv_file_acc_path_sleeping_right_tilt)
df_gyro_sleeping_right_tilt = read_gyro(csv_file_gyro_path_sleeping_right_tilt)

"""## Left Tilt"""
csv_file_acc_path_left_tilt = 'app://Left tilt/Accelerometer.csv'
csv_file_gyro_path_left_tilt = 'app://Left tilt/Gyroscope.csv'
df_acc_left_tilt = read_acc(csv_file_acc_path_left_tilt)
df_gyro_left_tilt = read_gyro(csv_file_gyro_path_left_tilt)

"""## Sleeping Left Tilt"""
csv_file_acc_path_sleeping_left_tilt = 'app://Sleeping Left Tilt/Accelerometer.csv'
csv_file_gyro_path_sleeping_left_tilt = 'app://Sleeping Left Tilt/Gyroscope.csv'
df_acc_sleeping_left_tilt = read_acc(csv_file_acc_path_sleeping_left_tilt)
df_gyro_sleeping_left_tilt = read_gyro(csv_file_gyro_path_sleeping_left_tilt)

"""## Front nod"""
csv_file_acc_path_front_nod = 'app://Front nod/Accelerometer.csv'
csv_file_gyro_path_front_nod = 'app://Front nod/Gyroscope.csv'
df_acc_front_nod = read_acc(csv_file_acc_path_front_nod)
df_gyro_front_nod = read_gyro(csv_file_gyro_path_front_nod)

"""## Back nod"""
csv_file_acc_path_back_nod = 'app://Back nod/Accelerometer.csv'
csv_file_gyro_path_back_nod = 'app://Back nod/Gyroscope.csv'
df_acc_back_nod = read_acc(csv_file_acc_path_back_nod)
df_gyro_back_nod = read_gyro(csv_file_gyro_path_back_nod)

"""# Low pass filter"""
def butter_lowpass_filter(data, cutoff=5, fs=100, order=15):
    nyquist = 0.5 * fs
    normal_cutoff = cutoff / nyquist
    b, a = butter(order, normal_cutoff, btype='low', analog=False)
    return filtfilt(b, a, data)

"""## Right Tilt"""
df_acc_right_tilt_filtered = pd.DataFrame({
    'elapsed (s)': df_acc_right_tilt['elapsed (s)'],
    'x-axis (g)': butter_lowpass_filter(df_acc_right_tilt['x-axis (g)']),
    'y-axis (g)': butter_lowpass_filter(df_acc_right_tilt['y-axis (g)']),
    'z-axis (g)': butter_lowpass_filter(df_acc_right_tilt['z-axis (g)'])
})
df_gyro_right_tilt_filtered = pd.DataFrame({
    'elapsed (s)': df_gyro_right_tilt['elapsed (s)'],
    'x-axis (deg/s)': butter_lowpass_filter(df_gyro_right_tilt['x-axis (deg/s)']),
    'y-axis (deg/s)': butter_lowpass_filter(df_gyro_right_tilt['y-axis (deg/s)']),
    'z-axis (deg/s)': butter_lowpass_filter(df_gyro_right_tilt['z-axis (deg/s)'])
})

"""## Test Filtered Left Right Tilt"""
df_acc_test_left_right_tilt_filtered = pd.DataFrame({
    'elapsed (s)': df_acc_test_left_right_tilt['elapsed (s)'],
    'x-axis (g)': butter_lowpass_filter(df_acc_test_left_right_tilt['x-axis (g)']),
    'y-axis (g)': butter_lowpass_filter(df_acc_test_left_right_tilt['y-axis (g)']),
    'z-axis (g)': butter_lowpass_filter(df_acc_test_left_right_tilt['z-axis (g)'])
})
df_gyro_test_left_right_tilt_filtered = pd.DataFrame({
    'elapsed (s)': df_gyro_test_left_right_tilt['elapsed (s)'],
    'x-axis (deg/s)': butter_lowpass_filter(df_gyro_test_left_right_tilt['x-axis (deg/s)']),
    'y-axis (deg/s)': butter_lowpass_filter(df_gyro_test_left_right_tilt['y-axis (deg/s)']),
    'z-axis (deg/s)': butter_lowpass_filter(df_gyro_test_left_right_tilt['z-axis (deg/s)'])
})

"""## Left Tilt"""
df_acc_left_tilt_filtered = pd.DataFrame({
    'elapsed (s)': df_acc_left_tilt['elapsed (s)'],
    'x-axis (g)': butter_lowpass_filter(df_acc_left_tilt['x-axis (g)']),
    'y-axis (g)': butter_lowpass_filter(df_acc_left_tilt['y-axis (g)']),
    'z-axis (g)': butter_lowpass_filter(df_acc_left_tilt['z-axis (g)'])
})
df_gyro_left_tilt_filtered = pd.DataFrame({
    'elapsed (s)': df_gyro_left_tilt['elapsed (s)'],
    'x-axis (deg/s)': butter_lowpass_filter(df_gyro_left_tilt['x-axis (deg/s)']),
    'y-axis (deg/s)': butter_lowpass_filter(df_gyro_left_tilt['y-axis (deg/s)']),
    'z-axis (deg/s)': butter_lowpass_filter(df_gyro_left_tilt['z-axis (deg/s)'])
})

"""## Front nod"""
df_acc_front_nod_filtered = pd.DataFrame({
    'elapsed (s)': df_acc_front_nod['elapsed (s)'],
    'x-axis (g)': butter_lowpass_filter(df_acc_front_nod['x-axis (g)']),
    'y-axis (g)': butter_lowpass_filter(df_acc_front_nod['y-axis (g)']),
    'z-axis (g)': butter_lowpass_filter(df_acc_front_nod['z-axis (g)'])
})
df_gyro_front_nod_filtered = pd.DataFrame({
    'elapsed (s)': df_gyro_front_nod['elapsed (s)'],
    'x-axis (deg/s)': butter_lowpass_filter(df_gyro_front_nod['x-axis (deg/s)']),
    'y-axis (deg/s)': butter_lowpass_filter(df_gyro_front_nod['y-axis (deg/s)']),
    'z-axis (deg/s)': butter_lowpass_filter(df_gyro_front_nod['z-axis (deg/s)'])
})

"""## Back nod"""
df_acc_back_nod_filtered = pd.DataFrame({
    'elapsed (s)': df_acc_back_nod['elapsed (s)'],
    'x-axis (g)': butter_lowpass_filter(df_acc_back_nod['x-axis (g)']),
    'y-axis (g)': butter_lowpass_filter(df_acc_back_nod['y-axis (g)']),
    'z-axis (g)': butter_lowpass_filter(df_acc_back_nod['z-axis (g)'])
})
df_gyro_back_nod_filtered = pd.DataFrame({
    'elapsed (s)': df_gyro_back_nod['elapsed (s)'],
    'x-axis (deg/s)': butter_lowpass_filter(df_gyro_back_nod['x-axis (deg/s)']),
    'y-axis (deg/s)': butter_lowpass_filter(df_gyro_back_nod['y-axis (deg/s)']),
    'z-axis (deg/s)': butter_lowpass_filter(df_gyro_back_nod['z-axis (deg/s)'])
})


"""# Template construction"""

from enum import Enum

class Match(Enum):
    RIGHT_TILT = 1
    LEFT_TILT = 2
    FRONT_NOD = 3
    BACK_NOD = 4

def strToMatch(str):
  ans = []
  for char in str:
    if char == 'R':
      ans.append(Match.RIGHT_TILT)
    elif char == 'L':
      ans.append(Match.LEFT_TILT)
    elif char == 'U':
      ans.append(Match.FRONT_NOD)
    elif char == 'D':
      ans.append(Match.BACK_NOD)
  return ans

"""## Right tilt template construction"""

csv_file_acc_path_single_right_tilt = 'app://Single right tilt/Accelerometer.csv'
csv_file_gyro_path_single_right_tilt = 'app://Single right tilt/Gyroscope.csv'
df_acc_single_right_tilt = read_acc(csv_file_acc_path_single_right_tilt)
df_gyro_single_right_tilt = read_gyro(csv_file_gyro_path_single_right_tilt)
df_acc_single_right_tilt = df_acc_single_right_tilt[(df_acc_single_right_tilt['elapsed (s)'] > 1.4) &
                                          (df_acc_single_right_tilt['elapsed (s)'] < 2.9)]
df_gyro_single_right_tilt = df_gyro_single_right_tilt[(df_gyro_single_right_tilt['elapsed (s)'] > 1.4) &
                                            (df_gyro_single_right_tilt['elapsed (s)'] < 2.9)]
df_acc_single_right_tilt_filtered = pd.DataFrame({
    'elapsed (s)': df_acc_single_right_tilt['elapsed (s)'],
    'x-axis (g)': butter_lowpass_filter(df_acc_single_right_tilt['x-axis (g)']),
    'y-axis (g)': butter_lowpass_filter(df_acc_single_right_tilt['y-axis (g)']),
    'z-axis (g)': butter_lowpass_filter(df_acc_single_right_tilt['z-axis (g)'])
})
df_gyro_single_right_tilt_filtered = pd.DataFrame({
    'elapsed (s)': df_gyro_single_right_tilt['elapsed (s)'],
    'x-axis (deg/s)': butter_lowpass_filter(df_gyro_single_right_tilt['x-axis (deg/s)']),
    'y-axis (deg/s)': butter_lowpass_filter(df_gyro_single_right_tilt['y-axis (deg/s)']),
    'z-axis (deg/s)': butter_lowpass_filter(df_gyro_single_right_tilt['z-axis (deg/s)'])
})
template_right_tilt = {
    'data':pd.DataFrame({
    'elapsed (s)': df_acc_single_right_tilt_filtered['elapsed (s)'],
    'acc': df_acc_single_right_tilt_filtered['z-axis (g)'],
    'gyro': df_gyro_single_right_tilt_filtered['y-axis (deg/s)'],
    }),
    'type': Match.RIGHT_TILT
}

"""## Left tilt template construction"""
csv_file_acc_path_single_left_tilt = 'app://Single left tilt/Accelerometer.csv'
csv_file_gyro_path_single_left_tilt = 'app://Single left tilt/Gyroscope.csv'
df_acc_single_left_tilt = read_acc(csv_file_acc_path_single_left_tilt)
df_gyro_single_left_tilt = read_gyro(csv_file_gyro_path_single_left_tilt)
df_acc_single_left_tilt = df_acc_single_left_tilt[(df_acc_single_left_tilt['elapsed (s)'] > 1.25) &
                                          (df_acc_single_left_tilt['elapsed (s)'] < 2.75)]
df_gyro_single_left_tilt = df_gyro_single_left_tilt[(df_gyro_single_left_tilt['elapsed (s)'] > 1.25) &
                                            (df_gyro_single_left_tilt['elapsed (s)'] < 2.75)]
df_acc_single_left_tilt_filtered = pd.DataFrame({
    'elapsed (s)': df_acc_single_left_tilt['elapsed (s)'],
    'x-axis (g)': butter_lowpass_filter(df_acc_single_left_tilt['x-axis (g)']),
    'y-axis (g)': butter_lowpass_filter(df_acc_single_left_tilt['y-axis (g)']),
    'z-axis (g)': butter_lowpass_filter(df_acc_single_left_tilt['z-axis (g)'])
})
df_gyro_single_left_tilt_filtered = pd.DataFrame({
    'elapsed (s)': df_gyro_single_left_tilt['elapsed (s)'],
    'x-axis (deg/s)': butter_lowpass_filter(df_gyro_single_left_tilt['x-axis (deg/s)']),
    'y-axis (deg/s)': butter_lowpass_filter(df_gyro_single_left_tilt['y-axis (deg/s)']),
    'z-axis (deg/s)': butter_lowpass_filter(df_gyro_single_left_tilt['z-axis (deg/s)'])
})
template_left_tilt = {
    'data': pd.DataFrame({
    'elapsed (s)': df_acc_single_left_tilt_filtered['elapsed (s)'],
    'acc': df_acc_single_left_tilt_filtered['z-axis (g)'],
    'gyro': df_gyro_single_left_tilt_filtered['y-axis (deg/s)'],
      }),
    'type': Match.LEFT_TILT
}

"""## Front nod template construction"""
csv_file_acc_path_single_front_nod = 'app://Single front nod/Accelerometer.csv'
csv_file_gyro_path_single_front_nod = 'app://Single front nod/Gyroscope.csv'
df_acc_single_front_nod = read_acc(csv_file_acc_path_single_front_nod)
df_gyro_single_front_nod = read_gyro(csv_file_gyro_path_single_front_nod)
df_acc_single_front_nod = df_acc_single_front_nod[(df_acc_single_front_nod['elapsed (s)'] > 1.25) &
                                          (df_acc_single_front_nod['elapsed (s)'] < 2.75)]
df_gyro_single_front_nod = df_gyro_single_front_nod[(df_gyro_single_front_nod['elapsed (s)'] > 1.25) &
                                            (df_gyro_single_front_nod['elapsed (s)'] < 2.75)]
df_acc_single_front_nod_filtered = pd.DataFrame({
    'elapsed (s)': df_acc_single_front_nod['elapsed (s)'],
    'x-axis (g)': butter_lowpass_filter(df_acc_single_front_nod['x-axis (g)']),
    'y-axis (g)': butter_lowpass_filter(df_acc_single_front_nod['y-axis (g)']),
    'z-axis (g)': butter_lowpass_filter(df_acc_single_front_nod['z-axis (g)'])
})
df_gyro_single_front_nod_filtered = pd.DataFrame({
    'elapsed (s)': df_gyro_single_front_nod['elapsed (s)'],
    'x-axis (deg/s)': butter_lowpass_filter(df_gyro_single_front_nod['x-axis (deg/s)']),
    'y-axis (deg/s)': butter_lowpass_filter(df_gyro_single_front_nod['y-axis (deg/s)']),
    'z-axis (deg/s)': butter_lowpass_filter(df_gyro_single_front_nod['z-axis (deg/s)'])
})
template_front_nod = {
    'data': pd.DataFrame({
    'elapsed (s)': df_acc_single_front_nod_filtered['elapsed (s)'],
    'acc': df_acc_single_front_nod_filtered['y-axis (g)'],
    'gyro': df_gyro_single_front_nod_filtered['z-axis (deg/s)'],
      }),
    'type': Match.FRONT_NOD
}

"""## Back nod template construction"""
csv_file_acc_path_single_back_nod = 'app://Single back nod/Accelerometer.csv'
csv_file_gyro_path_single_back_nod = 'app://Single back nod/Gyroscope.csv'
df_acc_single_back_nod = read_acc(csv_file_acc_path_single_back_nod)
df_gyro_single_back_nod = read_gyro(csv_file_gyro_path_single_back_nod)
df_acc_single_back_nod = df_acc_single_back_nod[(df_acc_single_back_nod['elapsed (s)'] >= 1.25) &
                                          (df_acc_single_back_nod['elapsed (s)'] <= 2.73)]
df_gyro_single_back_nod = df_gyro_single_back_nod[(df_gyro_single_back_nod['elapsed (s)'] >= 1.25) &
                                            (df_gyro_single_back_nod['elapsed (s)'] <= 2.73)]
df_acc_single_back_nod_filtered = pd.DataFrame({
    'elapsed (s)': df_acc_single_back_nod['elapsed (s)'],
    'x-axis (g)': butter_lowpass_filter(df_acc_single_back_nod['x-axis (g)']),
    'y-axis (g)': butter_lowpass_filter(df_acc_single_back_nod['y-axis (g)']),
    'z-axis (g)': butter_lowpass_filter(df_acc_single_back_nod['z-axis (g)'])
})
df_gyro_single_back_nod_filtered = pd.DataFrame({
    'elapsed (s)': df_gyro_single_back_nod['elapsed (s)'],
    'x-axis (deg/s)': butter_lowpass_filter(df_gyro_single_back_nod['x-axis (deg/s)']),
    'y-axis (deg/s)': butter_lowpass_filter(df_gyro_single_back_nod['y-axis (deg/s)']),
    'z-axis (deg/s)': butter_lowpass_filter(df_gyro_single_back_nod['z-axis (deg/s)'])
})
template_back_nod = {
    'data': pd.DataFrame({
    'elapsed (s)': df_acc_single_back_nod_filtered['elapsed (s)'],
    'acc': df_acc_single_back_nod_filtered['y-axis (g)'],
    'gyro': df_gyro_single_back_nod_filtered['z-axis (deg/s)'],
      }),
    'type': Match.BACK_NOD
}

"""# Verification matching"""
from fastdtw import fastdtw
from scipy.spatial.distance import euclidean

window_size = min(len(template_right_tilt['data']), len(template_left_tilt['data']))
step_size = 25
skip_size = window_size // 2
start = 0
threshold = 22.00
ratio_acc_gyro = 2/5

templates = [template_right_tilt, template_left_tilt, template_front_nod, template_back_nod]

def acc_axis(type, window):
  if type.value < 3:
    return window['z-axis (g)'].to_numpy().reshape(1,-1)
  else:
    return window['y-axis (g)'].to_numpy().reshape(1,-1)

def gyro_axis(type, window):
  if type.value < 3:
    return window['y-axis (deg/s)'].to_numpy().reshape(1,-1)
  else:
    return window['z-axis (deg/s)'].to_numpy().reshape(1,-1)

def matcher(window_acc, window_gyro, start, plot = False):

  matches = []

  for template in templates:
    template_acc = template['data']['acc'].to_numpy().reshape(1,-1)
    template_gyro = template['data']['gyro'].to_numpy().reshape(1,-1)

    acc_distance, _ = fastdtw(template_acc, acc_axis(template['type'], window_acc), dist=euclidean)
    gyro_distance, _ = fastdtw(template_gyro, gyro_axis(template['type'], window_gyro), dist=euclidean)

    combined_distance = ((ratio_acc_gyro) * acc_distance + (1 - ratio_acc_gyro) * gyro_distance)

    if combined_distance < threshold:
      matches.append({
        'start_index': start,
        'end_index': start + window_size,
        'dtw_distance': combined_distance,
        'type': template['type']
      })

  final_ans = None
  if len(matches)>0:
    print(matches)
    for match in matches:
      print(final_ans)
      if final_ans is None:
        final_ans = match
      elif final_ans.dtw_distance > match.dtw_distance:
        final_ans = match
  return final_ans


def match_tilt(data_acc, data_gyro, plot=False):

  matches = []

  start = 0

  while start <= min(len(data_acc), len(data_gyro)) - window_size:

    last_checked_index = start
    window_acc = data_acc.iloc[start:start+window_size]
    window_gyro = data_gyro.iloc[start:start+window_size]

    matched = matcher(window_acc, window_gyro, start, plot)

    if matched != None:
      matches.append(matched)
      # print(f"Matched {start}, now {start + skip_size}")
      start += skip_size
    else:
      start += step_size

  return matches

def match_tilt_from_kotlin(acc_path, gyro_path, plot=False):
    # Read and process the data
    df_acc = read_acc(acc_path)
    df_gyro = read_gyro(gyro_path)

    # Apply the low-pass filter
    df_acc_filtered = pd.DataFrame({
        'elapsed (s)': df_acc['elapsed (s)'],
        'x-axis (g)': butter_lowpass_filter(df_acc['x-axis (g)']),
        'y-axis (g)': butter_lowpass_filter(df_acc['y-axis (g)']),
        'z-axis (g)': butter_lowpass_filter(df_acc['z-axis (g)'])
    })

    df_gyro_filtered = pd.DataFrame({
        'elapsed (s)': df_gyro['elapsed (s)'],
        'x-axis (deg/s)': butter_lowpass_filter(df_gyro['x-axis (deg/s)']),
        'y-axis (deg/s)': butter_lowpass_filter(df_gyro['y-axis (deg/s)']),
        'z-axis (deg/s)': butter_lowpass_filter(df_gyro['z-axis (deg/s)'])
    })

    # Perform the matching
    return match_tilt(df_acc_filtered, df_gyro_filtered, plot)



# Matching Right tilts
# match_tilt(df_acc_right_tilt_filtered, df_gyro_right_tilt_filtered, False)
# Matching left tilts
# match_tilt(df_acc_left_tilt_filtered, df_gyro_left_tilt_filtered, False)
# Matching front nod
# match_tilt(df_acc_front_nod_filtered, df_gyro_front_nod_filtered, False)
# Matching back nod
# match_tilt(df_acc_back_nod_filtered, df_gyro_back_nod_filtered, False)
