import time

input_csv = 'student_data.csv'
output_txt = '2353250-hw2.txt'

start_time = time.time()

with open(input_csv, 'r', encoding='utf-8') as fin, open(output_txt, 'w', encoding='utf-8') as fout:
    for line in fin:
        # 去除行尾换行符，重新加上换行
        fout.write(line.rstrip('\n') + '\n')

end_time = time.time()
elapsed_time_ms = (end_time - start_time) * 1000

print(f"文件生成所需时间: {elapsed_time_ms:.2f} 毫秒")
