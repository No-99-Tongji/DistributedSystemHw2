import java.io.*;
import java.util.*;

public class QueryByChineseScoreRange {

    // 学生数据类
    static class Student {
        int studentId;
        float chineseScore;
        float mathScore;
        float englishScore;
        float comprehensiveScore;

        @Override
        public String toString() {
            return String.format("学号: %d, 语文: %.1f, 数学: %.1f, 英语: %.1f, 综合: %.1f",
                studentId, chineseScore, mathScore, englishScore, comprehensiveScore);
        }
    }

    // 索引节点类
    static class IndexNode {
        int chineseScoreInt;  // 语文成绩整数值
        long bytePosition;    // 文件中的字节位置

        public IndexNode(int chineseScoreInt, long bytePosition) {
            this.chineseScoreInt = chineseScoreInt;
            this.bytePosition = bytePosition;
        }
    }

    // 查询结果类
    static class QueryResult {
        int studentCount;
        double averageScore;
        long queryTime;
        List<Student> students;

        public QueryResult(int studentCount, double averageScore, long queryTime, List<Student> students) {
            this.studentCount = studentCount;
            this.averageScore = averageScore;
            this.queryTime = queryTime;
            this.students = students;
        }
    }

    /**
     * 从索引文件中读取所有索引节点
     */
    private static List<IndexNode> loadIndexFile(String indexFile) throws IOException {
        List<IndexNode> indexNodes = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(indexFile);
             DataInputStream dis = new DataInputStream(new BufferedInputStream(fis))) {

            // 读取元数据
            int nodeCount = dis.readInt();
            int treeHeight = dis.readInt();

            System.out.println("索引文件信息:");
            System.out.println("  节点总数: " + nodeCount);
            System.out.println("  树高度: " + treeHeight);

            // 读取所有节点
            for (int i = 0; i < nodeCount; i++) {
                int scoreInt = dis.readInt();
                long position = dis.readLong();
                indexNodes.add(new IndexNode(scoreInt, position));
            }
        }

        return indexNodes;
    }

    /**
     * 根据语文成绩范围查询学生信息
     *
     * @param minScore 最小语文成绩（包含）
     * @param maxScore 最大语文成绩（包含）
     * @return 查询结果
     */
    public static QueryResult queryByChineseScoreRange(float minScore, float maxScore) throws IOException {
        long startTime = System.currentTimeMillis();

        String indexFile = "2353250-hw2.idx";
        String datFile = "2353250-hw2.dat2";

        // 检查文件是否存在
        if (!new File(indexFile).exists()) {
            throw new FileNotFoundException("索引文件不存在: " + indexFile);
        }
        if (!new File(datFile).exists()) {
            throw new FileNotFoundException("数据文件不存在: " + datFile);
        }

        // 1. 读取索引文件
        List<IndexNode> indexNodes = loadIndexFile(indexFile);

        // 2. 计算成绩整数值范围
        // 最大小于等于 minScore 的整数
        int minScoreInt = (int) minScore;
        // 最小大于等于 maxScore 的整数
        int maxScoreInt = (int) Math.ceil(maxScore);

        System.out.println("\n查询范围:");
        System.out.println("  输入成绩范围: [" + minScore + ", " + maxScore + "]");
        System.out.println("  整数范围: [" + minScoreInt + ", " + maxScoreInt + "]");

        // 3. 从索引中找到对应的字节位置范围
        long startBytePos = -1;
        long endBytePos = -1;

        // 找到 >= minScoreInt 的最小整数对应的位置（作为起始位置）
        for (IndexNode node : indexNodes) {
            if (node.chineseScoreInt >= minScoreInt) {
                startBytePos = node.bytePosition;
                break;
            }
        }

        // 找到 > maxScoreInt 的最小整数对应的位置（作为结束位置的上界）
        // 如果找不到，说明要读到文件末尾
        for (IndexNode node : indexNodes) {
            if (node.chineseScoreInt > maxScoreInt) {
                endBytePos = node.bytePosition;
                break;
            }
        }

        if (startBytePos == -1) {
            // 没有符合条件的数据
            long queryTime = System.currentTimeMillis() - startTime;
            return new QueryResult(0, 0.0, queryTime, new ArrayList<>());
        }

        System.out.println("\n索引查找结果:");
        System.out.println("  起始字节位置: " + startBytePos);
        System.out.println("  结束字节位置: " + (endBytePos == -1 ? "文件末尾" : endBytePos));

        // 4. 使用随机访问方式读取文件中的数据
        List<Student> studentsInRange = new ArrayList<>();
        double totalScore = 0;

        try (RandomAccessFile raf = new RandomAccessFile(datFile, "r")) {
            // 跳转到起始位置
            raf.seek(startBytePos);

            while (true) {
                // 检查是否到达结束位置
                long currentPos = raf.getFilePointer();
                if (endBytePos != -1 && currentPos >= endBytePos) {
                    break;
                }

                // 检查是否到达文件末尾
                if (currentPos + 20 > raf.length()) {
                    break;
                }

                // 读取学生信息
                int studentId = raf.readInt();
                float chineseScore = raf.readFloat();
                float mathScore = raf.readFloat();
                float englishScore = raf.readFloat();
                float comprehensiveScore = raf.readFloat();

                // 检查是否在成绩范围内（包含边界）
                if (chineseScore >= minScore && chineseScore <= maxScore) {
                    Student student = new Student();
                    student.studentId = studentId;
                    student.chineseScore = chineseScore;
                    student.mathScore = mathScore;
                    student.englishScore = englishScore;
                    student.comprehensiveScore = comprehensiveScore;

                    studentsInRange.add(student);
                    totalScore += chineseScore;
                }

                // 如果当前成绩的整数部分已经超过最大范围，可以提前退出
                // 因为文件是按语文成绩从高到低排序的
                if ((int) chineseScore < minScoreInt) {
                    break;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long queryTime = endTime - startTime;

        // 计算平均成绩
        double averageScore = studentsInRange.isEmpty() ? 0.0 : totalScore / studentsInRange.size();

        return new QueryResult(studentsInRange.size(), averageScore, queryTime, studentsInRange);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== 语文成绩范围查询系统 ===");
            System.out.println("索引文件: 2353250-hw2.idx");
            System.out.println("数据文件: 2353250-hw2.dat2");
            System.out.println();

            System.out.print("请输入最小语文成绩（例如 89.5）: ");
            float minScore = scanner.nextFloat();

            System.out.print("请输入最大语文成绩（例如 92.1）: ");
            float maxScore = scanner.nextFloat();

            if (minScore > maxScore) {
                System.out.println("错误: 最小成绩不能大于最大成绩");
                return;
            }

            System.out.println("\n正在查询语文成绩在 [" + minScore + ", " + maxScore + "] 范围内的学生...\n");
            System.out.println("=".repeat(60));

            QueryResult result = queryByChineseScoreRange(minScore, maxScore);

            System.out.println("\n=== 查询结果 ===");
            System.out.println("学生数量: " + result.studentCount);
            System.out.printf("平均成绩: %.2f\n", result.averageScore);
            System.out.println("查询耗时: " + result.queryTime + " 毫秒");

            // 显示前10个和后10个学生
            if (result.studentCount > 0) {
                System.out.println("\n前" + Math.min(10, result.studentCount) + "个学生信息:");
                for (int i = 0; i < Math.min(10, result.studentCount); i++) {
                    System.out.println((i + 1) + ". " + result.students.get(i));
                }

                if (result.studentCount > 20) {
                    System.out.println("...");
                    System.out.println("\n后10个学生信息:");
                    for (int i = Math.max(10, result.studentCount - 10); i < result.studentCount; i++) {
                        System.out.println((i + 1) + ". " + result.students.get(i));
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("输入错误，请输入有效的成绩（浮点数）");
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}

