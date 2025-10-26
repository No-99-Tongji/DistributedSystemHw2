import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BuildIndexTree {

    // 学生数据类
    static class Student {
        int studentId;
        float chineseScore;
        float mathScore;
        float englishScore;
        float comprehensiveScore;

        public Student(int studentId, float chineseScore, float mathScore,
                      float englishScore, float comprehensiveScore) {
            this.studentId = studentId;
            this.chineseScore = chineseScore;
            this.mathScore = mathScore;
            this.englishScore = englishScore;
            this.comprehensiveScore = comprehensiveScore;
        }

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

    // AVL树节点
    static class AVLNode {
        int chineseScoreInt;  // 语文成绩整数值（索引键）
        long bytePosition;    // 该成绩第一个学生在文件中的字节位置
        AVLNode left;
        AVLNode right;
        int height;

        public AVLNode(int chineseScoreInt, long bytePosition) {
            this.chineseScoreInt = chineseScoreInt;
            this.bytePosition = bytePosition;
            this.left = null;
            this.right = null;
            this.height = 1;
        }
    }

    // AVL树类
    static class AVLTree {
        private AVLNode root;

        // 获取节点高度
        private int height(AVLNode node) {
            return node == null ? 0 : node.height;
        }

        // 更新节点高度
        private void updateHeight(AVLNode node) {
            if (node != null) {
                node.height = 1 + Math.max(height(node.left), height(node.right));
            }
        }

        // 获取平衡因子
        private int getBalance(AVLNode node) {
            return node == null ? 0 : height(node.left) - height(node.right);
        }

        // 右旋转
        private AVLNode rotateRight(AVLNode y) {
            AVLNode x = y.left;
            AVLNode T2 = x.right;

            x.right = y;
            y.left = T2;

            updateHeight(y);
            updateHeight(x);

            return x;
        }

        // 左旋转
        private AVLNode rotateLeft(AVLNode x) {
            AVLNode y = x.right;
            AVLNode T2 = y.left;

            y.left = x;
            x.right = T2;

            updateHeight(x);
            updateHeight(y);

            return y;
        }

        // 插入节点
        public void insert(int chineseScoreInt, long bytePosition) {
            root = insertNode(root, chineseScoreInt, bytePosition);
        }

        private AVLNode insertNode(AVLNode node, int chineseScoreInt, long bytePosition) {
            // 1. 执行标准BST插入
            if (node == null) {
                return new AVLNode(chineseScoreInt, bytePosition);
            }

            if (chineseScoreInt < node.chineseScoreInt) {
                node.left = insertNode(node.left, chineseScoreInt, bytePosition);
            } else if (chineseScoreInt > node.chineseScoreInt) {
                node.right = insertNode(node.right, chineseScoreInt, bytePosition);
            } else {
                // 如果键已存在，不更新（保留第一个学生的位置）
                return node;
            }

            // 2. 更新节点高度
            updateHeight(node);

            // 3. 获取平衡因子
            int balance = getBalance(node);

            // 4. 如果不平衡，进行旋转
            // Left Left Case
            if (balance > 1 && chineseScoreInt < node.left.chineseScoreInt) {
                return rotateRight(node);
            }

            // Right Right Case
            if (balance < -1 && chineseScoreInt > node.right.chineseScoreInt) {
                return rotateLeft(node);
            }

            // Left Right Case
            if (balance > 1 && chineseScoreInt > node.left.chineseScoreInt) {
                node.left = rotateLeft(node.left);
                return rotateRight(node);
            }

            // Right Left Case
            if (balance < -1 && chineseScoreInt < node.right.chineseScoreInt) {
                node.right = rotateRight(node.right);
                return rotateLeft(node);
            }

            return node;
        }

        // 中序遍历（用于序列化）
        public void inorderTraversal(List<AVLNode> result) {
            inorderHelper(root, result);
        }

        private void inorderHelper(AVLNode node, List<AVLNode> result) {
            if (node != null) {
                inorderHelper(node.left, result);
                result.add(node);
                inorderHelper(node.right, result);
            }
        }

        // 获取节点总数
        public int getNodeCount() {
            return getNodeCountHelper(root);
        }

        private int getNodeCountHelper(AVLNode node) {
            if (node == null) return 0;
            return 1 + getNodeCountHelper(node.left) + getNodeCountHelper(node.right);
        }

        // 获取树高度
        public int getTreeHeight() {
            return height(root);
        }
    }

    /**
     * 构建二叉平衡树索引
     * 读取已存在的"2353250-hw2.dat2"文件，对语文成绩的整数值进行索引，
     * 保存每个成绩整数值对应的第一个学生在文件中的字节位置
     *
     * @return 文件生成所需时间（单位：毫秒）
     */
    public static long buildIndexTree() throws IOException {
        long startTime = System.currentTimeMillis();

        // 数据文件（由MergeSortStudentData生成，已按语文成绩从高到低排序）
        String datFile = "2353250-hw2.dat2";

        // 检查数据文件是否存在
        File dataFile = new File(datFile);
        if (!dataFile.exists()) {
            throw new FileNotFoundException("数据文件不存在: " + datFile + "\n请先运行 MergeSortStudentData 生成该文件");
        }

        AVLTree indexTree = new AVLTree();
        Map<Integer, Long> scorePositionMap = new LinkedHashMap<>();

        // 读取已存在的 dat2 文件，构建索引
        try (FileInputStream fis = new FileInputStream(datFile);
             DataInputStream dis = new DataInputStream(new BufferedInputStream(fis))) {

            long currentPosition = 0;
            int studentCount = 0;

            try {
                while (true) {
                    // 读取学生信息（20字节）
                    int studentId = dis.readInt();
                    float chineseScore = dis.readFloat();
                    float mathScore = dis.readFloat();
                    float englishScore = dis.readFloat();
                    float comprehensiveScore = dis.readFloat();

                    // 获取语文成绩的整数值（截断，不四舍五入）
                    int chineseScoreInt = (int) chineseScore;

                    // 如果这是该成绩整数值的第一个学生，记录位置
                    if (!scorePositionMap.containsKey(chineseScoreInt)) {
                        scorePositionMap.put(chineseScoreInt, currentPosition);
                    }

                    currentPosition += 20; // 每条记录20字节
                    studentCount++;
                }
            } catch (EOFException e) {
                // 文件读取完毕
                System.out.println("读取完成，共 " + studentCount + " 条学生记录");
            }
        }

        // 将成绩-位置映射插入AVL树
        for (Map.Entry<Integer, Long> entry : scorePositionMap.entrySet()) {
            indexTree.insert(entry.getKey(), entry.getValue());
        }

        // 将AVL树序列化到索引文件
        String indexFile = "2353250-hw2.idx";

        try (FileOutputStream fos = new FileOutputStream(indexFile);
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos))) {

            // 写入树的元数据
            dos.writeInt(indexTree.getNodeCount());  // 节点总数
            dos.writeInt(indexTree.getTreeHeight()); // 树高度

            // 中序遍历获取所有节点
            List<AVLNode> nodes = new ArrayList<>();
            indexTree.inorderTraversal(nodes);

            // 写入每个节点的数据（成绩整数值 + 字节位置）
            for (AVLNode node : nodes) {
                dos.writeInt(node.chineseScoreInt);  // 4字节：语文成绩整数值
                dos.writeLong(node.bytePosition);    // 8字节：文件中的字节位置
            }

            dos.flush();
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        // 打印结果
        System.out.println("\n=== 索引树构建完成 ===");
        System.out.println("源数据文件: " + datFile);
        System.out.println("索引文件: " + indexFile);
        System.out.println("索引节点数: " + indexTree.getNodeCount() + " (不同的语文成绩整数值)");
        System.out.println("树高度: " + indexTree.getTreeHeight());
        System.out.println("索引文件大小: " + (8 + indexTree.getNodeCount() * 12) + " 字节");
        System.out.println("生成耗时: " + elapsedTime + " 毫秒");

        // 显示部分索引信息（从高分到低分）
        System.out.println("\n索引示例（语文成绩从高到低，显示前10个）：");
        System.out.println("语文成绩\t文件字节位置");
        List<AVLNode> nodes = new ArrayList<>();
        indexTree.inorderTraversal(nodes);

        int displayCount = Math.min(10, nodes.size());
        for (int i = nodes.size() - 1; i >= Math.max(0, nodes.size() - displayCount); i--) {
            AVLNode node = nodes.get(i);
            System.out.println(node.chineseScoreInt + "\t\t" + node.bytePosition);
        }

        return elapsedTime;
    }

    public static void main(String[] args) {
        try {
            long elapsedTime = buildIndexTree();
            System.out.println("\n返回值: " + elapsedTime + " 毫秒");
        } catch (IOException e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

