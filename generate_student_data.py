import numpy as np
import pandas as pd

# 计算总行数
total_rows = 512 * 32 * 8  # 131,072 行

# 生成学号（从1开始递增）
student_ids = list(range(1, total_rows + 1))

# 使用高斯分布生成成绩（均值50，标准差15，然后裁剪到0-100范围）
np.random.seed(42)  # 设置随机种子以便复现

def generate_scores(size):
    """使用高斯分布生成0-100之间的分数，保留一位小数"""
    scores = np.random.normal(loc=50, scale=15, size=size)
    # 裁剪到0-100范围
    scores = np.clip(scores, 0.0, 100.0)
    # 保留一位小数
    scores = np.round(scores, 1)
    return scores

# 生成各科成绩
chinese_scores = generate_scores(total_rows)
math_scores = generate_scores(total_rows)
english_scores = generate_scores(total_rows)
comprehensive_scores = generate_scores(total_rows)

# 创建DataFrame
df = pd.DataFrame({
    '学号': student_ids,
    '语文成绩': chinese_scores,
    '数学成绩': math_scores,
    '英语成绩': english_scores,
    '综合成绩': comprehensive_scores
})

# 保存到CSV文件
output_file = 'student_data.csv'
df.to_csv(output_file, index=False, encoding='utf-8-sig')

print(f"数据生成完成！")
print(f"总行数: {total_rows}")
print(f"文件已保存至: {output_file}")
print(f"\n前10行数据预览:")
print(df.head(10))
print(f"\n数据统计信息:")
print(df.describe())

