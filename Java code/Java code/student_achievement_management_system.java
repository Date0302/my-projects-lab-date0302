import java.util.*;
class student {
    private String id;
    private String name;
    private double[] grades;
    private double average;
    public student(String id, String name, double[] grades) {
        this.id = id;
        this.name = name;
        this.grades = grades;
        calculateAverage();
    }
    private void calculateAverage() {
        double sum = 0;
        for (double grade : grades) {
            sum += grade;
        }
        this.average = sum / grades.length;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double[] getGrades() {
        return grades;
    }
    public double getAverage() {
        return average;
    }
}
public class student_achievement_management_system {
    private static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        System.out.println("============ 学生成绩管理系统 ============");
        // 输入学生数量
        System.out.print("请输入学生数量: ");
        int n = scanner.nextInt();
        // 输入课程数量
        System.out.print("请输入课程数量: ");
        int m = scanner.nextInt();
        scanner.nextLine();  // 清除换行符
        List<Student> students = new ArrayList<>();
        // 输入学生信息
        for (int i = 0; i < n; i++) {
            System.out.println("\n--- 输入第 " + (i+1) + " 个学生信息 ---");
            System.out.print("学号: ");
            String id = scanner.nextLine();
            System.out.print("姓名: ");
            String name = scanner.nextLine();
            double[] grades = new double[m];
            for (int j = 0; j < m; j++) {
                System.out.print("课程 " + (j+1) + " 成绩: ");
                grades[j] = scanner.nextDouble();
            }
            scanner.nextLine();  // 清除换行符
            students.add(new Student(id, name, grades));
        }
        // 按平均成绩降序排序
        Collections.sort(students, (s1, s2) ->
                Double.compare(s2.getAverage(), s1.getAverage()));
        // 输出成绩表
        System.out.println("\n============== 成绩表 (按平均分降序) ==============");
        System.out.printf("%-10s %-10s", "学号", "姓名");
        for (int i = 0; i < m; i++) {
            System.out.printf("%10s", "课程" + (i+1));
        }
        System.out.printf("%12s\n", "平均分");
        for (Student student : students) {
            System.out.printf("%-10s %-10s", student.getId(), student.getName());
            for (double grade : student.getGrades()) {
                System.out.printf("%10.1f", grade);
            }
            System.out.printf("%12.1f\n", student.getAverage());
        }
        // 计算各科统计信息
        System.out.println("\n============== 各科成绩统计 ==============");
        System.out.printf("%-10s %-10s %-10s\n", "课程", "平均分", "最高分", "最低分");
        for (int i = 0; i < m; i++) {
            final int courseIndex = i;
            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (Student s : students) {
                double grade = s.getGrades()[courseIndex];
                sum += grade;
                if (grade < min) min = grade;
                if (grade > max) max = grade;
            }
            double avg = sum / students.size();
            System.out.printf("%-10s %-10.1f %-10.1f %-10.1f\n",
                    "课程" + (courseIndex+1), avg, max, min);
        }
        // 学生查询功能
        System.out.println("\n============== 学生成绩查询 ==============");
        while (true) {
            System.out.print("\n输入要查询的学生姓名 (输入 exit 退出系统): ");
            String name = scanner.nextLine();

            if ("exit".equalsIgnoreCase(name)) {
                System.out.println("感谢使用学生成绩管理系统！");
                break;
            }
            boolean found = false;
            for (Student s : students) {
                if (s.getName().equalsIgnoreCase(name)) {
                    System.out.println("--- 学生信息 ---");
                    System.out.println("学号: " + s.getId());
                    System.out.println("姓名: " + s.getName());
                    for (int i = 0; i < m; i++) {
                        System.out.printf("课程%d: %.1f\n", i+1, s.getGrades()[i]);
                    }
                    System.out.printf("平均分: %.1f\n", s.getAverage());
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("未找到学生: " + name);
            }
        }
    }
}