import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WriteStudentData {

    // 学生数据类
    static class Student implements Comparable<Student> {
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

        // 实现按学号从小到大排序
        @Override
        public int compareTo(Student other) {
            return Integer.compare(this.studentId, other.studentId);
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
     * 按照学号从小到大的顺序，将每个学生信息（5个字段）转化为字节数组byte[]，
     * 通过顺序写输出流的方式将该字节数组写入到数据文件"2353250-hw2.dat1"
     *
     * @return 文件生成所需时间（单位：毫秒）
     */
    public static long writeStudentDataToBinary() throws IOException {
        // 记录开始时间
        long startTime = System.currentTimeMillis();

        // 读取学生数据
        List<Student> students = new ArrayList<>();

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

                        students.add(new Student(studentId, chineseScore, mathScore,
                                                englishScore, comprehensiveScore));
                    } catch (NumberFormatException e) {
                        System.err.println("跳过无效行: " + line);
                    }
                }
            }
        }

        // 按学号从小到大排序
        Collections.sort(students);

        // 输出文件名
        String outputFile = "2353250-hw2.dat1";

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
        System.out.println("总记录数: " + students.size());
        System.out.println("每条记录大小: 20字节 (1个int + 4个float)");
        System.out.println("文件总大小: " + (students.size() * 20) + " 字节");
        System.out.println("生成耗时: " + elapsedTime + " 毫秒");

        return elapsedTime;
    }

    public static void main(String[] args) {
        try {
            long elapsedTime = writeStudentDataToBinary();
            System.out.println("\n返回值: " + elapsedTime + " 毫秒");
        } catch (IOException e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

