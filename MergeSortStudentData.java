import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MergeSortStudentData {

    // 学生数据类
    static class Student {
        int studentId;           // 学号
        float chineseScore;      // 语文成绩
        float mathScore;         // 数学成绩
        float englishScore;      // 英语成绩
        float comprehensiveScore; // 综合成绩

        public Student(int studentId, float chineseScore, float mathScore,
                      float englishScore, float comprehensiveScore) {
            this.studentId = studentId;
            this.chineseScore = chineseScore;
            this.mathScore = mathScore;
            this.englishScore = englishScore;
            this.comprehensiveScore = comprehensiveScore;
        }

        // 将学生信息转换为字节数组
        // 1个int(4字节) + 4个float(每个4字节) = 20字节
        public byte[] toByteArray() {
            ByteBuffer buffer = ByteBuffer.allocate(20);
            buffer.putInt(studentId);
            buffer.putFloat(chineseScore);
            buffer.putFloat(mathScore);
            buffer.putFloat(englishScore);
            buffer.putFloat(comprehensiveScore);
            return buffer.array();
        }
    }

    /**
     * 归并排序实现
     * 按照语文成绩从高到低排序，若成绩相同则按学号从小到大排序
     */
    private static void mergeSort(Student[] students, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            // 递归排序左半部分
            mergeSort(students, left, mid);

            // 递归排序右半部分
            mergeSort(students, mid + 1, right);

            // 合并两个有序部分
            merge(students, left, mid, right);
        }
    }

    /**
     * 合并两个有序数组
     */
    private static void merge(Student[] students, int left, int mid, int right) {
        // 创建临时数组
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Student[] leftArray = new Student[n1];
        Student[] rightArray = new Student[n2];

        // 复制数据到临时数组
        for (int i = 0; i < n1; i++) {
            leftArray[i] = students[left + i];
        }
        for (int j = 0; j < n2; j++) {
            rightArray[j] = students[mid + 1 + j];
        }

        // 合并临时数组
        int i = 0, j = 0;
        int k = left;

        while (i < n1 && j < n2) {
            // 比较规则：语文成绩从高到低，相同则学号从小到大
            if (compareStudents(leftArray[i], rightArray[j]) <= 0) {
                students[k] = leftArray[i];
                i++;
            } else {
                students[k] = rightArray[j];
                j++;
            }
            k++;
        }

        // 复制剩余元素
        while (i < n1) {
            students[k] = leftArray[i];
            i++;
            k++;
        }

        while (j < n2) {
            students[k] = rightArray[j];
            j++;
            k++;
        }
    }

    /**
     * 比较两个学生
     * 返回负数表示 s1 应该排在 s2 前面
     * 规则：语文成绩从高到低，相同则学号从小到大
     */
    private static int compareStudents(Student s1, Student s2) {
        // 先比较语文成绩（从高到低）
        if (s1.chineseScore != s2.chineseScore) {
            return Float.compare(s2.chineseScore, s1.chineseScore); // 注意顺序，高分在前
        }
        // 语文成绩相同，比较学号（从小到大）
        return Integer.compare(s1.studentId, s2.studentId);
    }

    /**
     * 通过merge-sort算法对语文成绩按照从高到低的顺序对所有学生信息进行排序，
     * 若有相同成绩的学生信息，则按照学号从小到大排序，
     * 将排序后的学生信息转化为字节数组byte[]，写到数据文件"2353250-hw2.dat2"
     *
     * @return 文件生成所需时间（单位：毫秒）
     */
    public static long sortAndWriteStudentData() throws IOException {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 读取学生数据
        List<Student> studentList = new ArrayList<>();

        // 读取CSV文件
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream("student_data.csv"), StandardCharsets.UTF_8))) {

            // 跳过表头（可能有BOM）
            String headerLine = br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                // 解析CSV行
                String[] fields = line.split(",");
                if (fields.length >= 5) {
                    try {
                        int studentId = Integer.parseInt(fields[0].trim());
                        float chineseScore = Float.parseFloat(fields[1].trim());
                        float mathScore = Float.parseFloat(fields[2].trim());
                        float englishScore = Float.parseFloat(fields[3].trim());
                        float comprehensiveScore = Float.parseFloat(fields[4].trim());

                        studentList.add(new Student(studentId, chineseScore, mathScore,
                                                   englishScore, comprehensiveScore));
                    } catch (NumberFormatException e) {
                        System.err.println("跳过无效行: " + line);
                    }
                }
            }
        }

        // 转换为数组以便进行归并排序
        Student[] students = studentList.toArray(new Student[0]);

        // 使用归并排序算法排序
        mergeSort(students, 0, students.length - 1);

        // 输出文件名
        String outputFile = "2353250-hw2.dat2";

        // 打开二进制文件进行写入
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            // 遍历每个学生，将其转换为字节数组并写入
            for (Student student : students) {
                byte[] byteArray = student.toByteArray();
                bos.write(byteArray);
            }

            // 确保所有数据写入磁盘
            bos.flush();
        }

        // 记录结束时间
        long endTime = System.currentTimeMillis();

        // 计算耗时（毫秒）
        long elapsedTime = endTime - startTime;

        // 打印结果
        System.out.println("文件生成完成: " + outputFile);
        System.out.println("总记录数: " + students.length);
        System.out.println("排序规则: 语文成绩从高到低，相同成绩按学号从小到大");
        System.out.println("每条记录大小: 20字节 (1个int + 4个float)");
        System.out.println("文件总大小: " + (students.length * 20) + " 字节");
        System.out.println("生成耗时: " + elapsedTime + " 毫秒");

        // 打印前10条数据作为验证
        System.out.println("\n前10条数据（验证排序结果）：");
        System.out.println("学号\t语文成绩\t数学成绩\t英语成绩\t综合成绩");
        for (int i = 0; i < Math.min(10, students.length); i++) {
            Student s = students[i];
            System.out.printf("%d\t%.1f\t%.1f\t%.1f\t%.1f\n",
                s.studentId, s.chineseScore, s.mathScore, s.englishScore, s.comprehensiveScore);
        }

        return elapsedTime;
    }

    public static void main(String[] args) {
        try {
            long elapsedTime = sortAndWriteStudentData();
            System.out.println("\n返回值: " + elapsedTime + " 毫秒");
        } catch (IOException e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

