import java.io.*;

public class QueryByStudentId {

    // 学生数据类
    static class Student {
        int studentId;
        float chineseScore;
        float mathScore;
        float englishScore;
        float comprehensiveScore;

        @Override
        public String toString() {
            return String.format("学号: %d\n语文成绩: %.1f\n数学成绩: %.1f\n英语成绩: %.1f\n综合成绩: %.1f",
                studentId, chineseScore, mathScore, englishScore, comprehensiveScore);
        }
    }

    /**
     * 从文件中读取指定学号的学生信息
     * 使用顺序查找方式
     *
     * @param studentId 要查询的学号
     * @return 包含学生信息和查询时间的结果
     */
    public static QueryResult queryStudentById(int studentId) throws IOException {
        long startTime = System.currentTimeMillis();

        String datFile = "2353250-hw2.dat1";

        // 检查文件是否存在
        File dataFile = new File(datFile);
        if (!dataFile.exists()) {
            throw new FileNotFoundException("数据文件不存在: " + datFile + "\n请先运行 WriteStudentData 生成该文件");
        }

        Student foundStudent = null;

        // 读取 dat1 文件（已按学号从小到大排序）
        try (FileInputStream fis = new FileInputStream(datFile);
             DataInputStream dis = new DataInputStream(new BufferedInputStream(fis))) {

            try {
                while (true) {
                    // 读取学生信息（20字节）
                    int id = dis.readInt();
                    float chineseScore = dis.readFloat();
                    float mathScore = dis.readFloat();
                    float englishScore = dis.readFloat();
                    float comprehensiveScore = dis.readFloat();

                    // 如果找到匹配的学号
                    if (id == studentId) {
                        foundStudent = new Student();
                        foundStudent.studentId = id;
                        foundStudent.chineseScore = chineseScore;
                        foundStudent.mathScore = mathScore;
                        foundStudent.englishScore = englishScore;
                        foundStudent.comprehensiveScore = comprehensiveScore;
                        break;
                    }

                    // 因为文件按学号从小到大排序，如果当前学号大于目标学号，可以提前退出
                    if (id > studentId) {
                        break;
                    }
                }
            } catch (EOFException e) {
                // 文件读取完毕
            }
        }

        long endTime = System.currentTimeMillis();
        long queryTime = endTime - startTime;

        return new QueryResult(foundStudent, queryTime);
    }

    // 查询结果类
    static class QueryResult {
        Student student;
        long queryTime;

        public QueryResult(Student student, long queryTime) {
            this.student = student;
            this.queryTime = queryTime;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("=== 学生信息查询系统 ===");
            System.out.println("数据文件: 2353250-hw2.dat1");
            System.out.println();

            // 检查命令行参数
            if (args.length != 1) {
                System.out.println("使用方法: java QueryByStudentId <学号>");
                System.out.println("例如: java QueryByStudentId 2023001");
                return;
            }

            int studentId = Integer.parseInt(args[0]);

            System.out.println("要查询的学号: " + studentId);
            System.out.println("\n正在查询学号 " + studentId + " 的信息...\n");

            QueryResult result = queryStudentById(studentId);

            if (result.student != null) {
                System.out.println("=== 查询结果 ===");
                System.out.println(result.student);
                System.out.println("\n查询耗时: " + result.queryTime + " 毫秒");
            } else {
                System.out.println("未找到学号为 " + studentId + " 的学生");
                System.out.println("查询耗时: " + result.queryTime + " 毫秒");
            }

        } catch (IOException e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("参数错误: 请输入有效的学号（整数）");
            System.err.println("使用方法: java QueryByStudentId <学号>");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("程序执行错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

