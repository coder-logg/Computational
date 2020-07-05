import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MathProcessor {
    private Map<String, Object> inputData;
    private List<List<Number>> extendedMatrix;
    private List<List<Number>> coeffMatrix;
    private Double eps;
    private Integer n;
    private List<Number> b_i;
    private Integer M;

    public MathProcessor(Map<String, Object> inputData) {
        this.inputData = inputData;
        n = (Integer) inputData.get("matrixOrder");
        extendedMatrix = new ArrayList<>((List<List<Number>>) inputData.get("matrix"));
        coeffMatrix = new ArrayList<>(extendedMatrix);
        for (int i = 0; i < n; i++) {
            coeffMatrix.add(i, new ArrayList<>());
            for (int j = 0; j < n; j++) {
                coeffMatrix.get(i).add(extendedMatrix.get(i).get(j));
            }
        }
        b_i = new ArrayList<>();
        eps = (Double) inputData.get("accuracy");
        M = (Integer) inputData.get("M");
    }

    public void solve() {
        List<List<Number>> reducedMatrix = makeReducedMatrix(extendedMatrix);
        if (reducedMatrix != null && checkConvergingByDiagonal(extendedMatrix)) {
            System.out.println("Изначальная матрица удовлетворяет условию сходимости.");
            iterate(reducedMatrix);
        } else {
//            if (reducedMatrix == null) {
//                System.out.println("К сожалению, решение найти невозможно.");
//                return;
//            }
            System.out.println("Изначальная матрица не удовлетворяет условию сходимости. Поиск подходящих преобразований для достижения диагонального преобладания.");
            System.out.println("Матрица до преобразования:");
            printMatrix(extendedMatrix);
            List<List<Number>> transformedMatrix = transformMatrix();
            if (transformedMatrix == null) {
                System.out.println("К сожалению, решение найти невозможно.");
                return;
            }
            System.out.println("Матрица после преобразования:");
            printMatrix(transformedMatrix);
            reducedMatrix = makeReducedMatrix(transformedMatrix);
            printMatrix(reducedMatrix);

            //Проверка достаточного условия сходимости
            if (checkConvergingByDiagonal(transformedMatrix)) {
                System.out.println("После преобразований матрица удовлетворяет условию сходимости.");
                iterate(reducedMatrix);
            } else {
                System.out.println("После преобразований матрица так и не удовлетворяет условия сходимости. К сожалению, решение найти невозможно.");
            }
        }
    }

    private void iterate(List<List<Number>> matrix) {
        List<List<Number>> coeffMatrix = new ArrayList<>();
        List<Number> b_i = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            b_i.add(matrix.get(i).get(n));
            coeffMatrix.add(matrix.get(i).stream().limit(n).collect(Collectors.toList()));
        }
        //itr(matrix, b_i);
        iterate(coeffMatrix, b_i);
    }


    private void iterate(List<List<Number>> coeffMatrix, List<Number> b_i) {
        List<Number> x_i = new ArrayList<>(b_i);
        double x;
        int iterationsCount = 0;
        double absErr = 0.0;
        double[] errors = new double[b_i.size()];
        printHeader(x_i);
        printItr(iterationsCount, x_i, absErr);
        do {
            absErr = 0.0;
            iterationsCount++;
            for (int i = 0; i < n; i++) {
                double s = 0.0;
                for (int j = 0; j < n; j++) {
                    if (i == j) continue;
                    s = s + coeffMatrix.get(i).get(j).doubleValue() * x_i.get(j).doubleValue();
                }
                x = (b_i.get(i).doubleValue() - s);// / (coeffMatrix.get(i).get(i).doubleValue());
                double d = Math.abs(x - x_i.get(i).doubleValue());
                errors[i] = d;
                if (d > absErr) {
                    absErr = d;
                }
                x_i.set(i, x);
            }
            printItr(iterationsCount, x_i, absErr);
            if (absErr < eps) {
                printResult(x_i, errors);
                break;
            }
            if (M != null && iterationsCount >= M) {
                System.out.println("Итерации расходятся");
                break;
            }
        } while (absErr > eps);
    }

    private void printResult(List<Number> x_i, double[] errors) {
        System.out.printf("%100s", "\n_______________Решение найдено_______________\n");
        System.out.printf("%-5s|%-32s|%-32s|%-32s\n", "x_i", "Решения", "Погрешности", "Невязки");
        for (int i = 0; i < x_i.size(); i++) {
            System.out.printf("%-5s|%-32.16f|%-32.16f|", "x_" + (i + 1), x_i.get(i).doubleValue(), errors[i]);
            double sum = 0.0;
            for (int j = 0; j < x_i.size(); j++) {
                sum += extendedMatrix.get(i).get(j).doubleValue() * x_i.get(j).doubleValue();
            }
            System.out.printf("%-32.16f\n", extendedMatrix.get(i).get(n).doubleValue() - sum);
        }
    }

    private void printMatrix(List<List<Number>> matrix) {
        matrix.forEach(o -> {
            o.forEach(number -> {
                System.out.printf("%-15f", number.doubleValue());
            });
            System.out.println();
        });
    }

    private void printHeader(List<Number> x_i) {
        System.out.printf("%3s|", "k");
        x_i.forEach(o -> System.out.printf("%-20.10s|", "x_" + (x_i.indexOf(o) + 1)));
        System.out.printf("%-20s", "Погрешности");
    }

    private void printItr(int counter, List<Number> x_i, double error) {
        System.out.printf("\n%3d|", counter);
        for (Number number : x_i) {
            System.out.printf("%-20.10f|", number.doubleValue());
        }
        System.out.printf("%-20.10f", error);

    }

//    private void itr(List<List<Number>> matrix, List<Number> b_i){
//        Double[] currentIterationValues = new Double[b_i.size()];
//        b_i.toArray(currentIterationValues);
//        Double[] previousIterationValues = currentIterationValues.clone();
//        //Number[][] matrix = new Integer[_matrix.size()][];
//       // coeffMatrix.toArray(matrix);
//        int iterationsCount = 0;
//        int n = previousIterationValues.length;
//        Double[] newValues = previousIterationValues.clone();
//        boolean isSolved = false;
//        while (!isSolved) {
//            previousIterationValues = currentIterationValues;
//            for (int i = 0; i < newValues.length; i++) {
//                double currentIterationSum = 0;
//                double previousIterationSum = 0;
//                for (int j = 0; j < newValues.length; j++) {
//                    if (j < i) {
//                        //Учет вклада промежуточных значений, полученных на текущей итерации
//                        currentIterationSum += matrix.get(i).get(j).doubleValue() * newValues[j];
//                    } else {
//                        //Учет вклада промежуточных значений, полученных на предыдушей итерации
//                        previousIterationSum += matrix.get(i).get(j).doubleValue() * newValues[j];
//                    }
//                }
//                //Получение нового промежуточного значения
//                double newValue = (matrix.get(i).get(n).doubleValue() - currentIterationSum - previousIterationSum);
//                newValues[i] = newValue;
//                System.out.println("x_" + iterationsCount + "_" + (i) + ">>>>" + newValues[i]);
//            }
//            currentIterationValues = newValues.clone();
//            isSolved = isSolved(currentIterationValues, previousIterationValues);
//            iterationsCount++;
//        }
//    }

    private boolean isSolved(Double[] currentIterationValues, Double[] previousIterationValues) {
        int n = currentIterationValues.length;
        double maxError = 0;

        for (int i = 0; i < n; i++) {
            double currentError = Math.abs(currentIterationValues[i] - previousIterationValues[i]);
            //Ищем максимальную погрешность среди всех искомых переменных
            if (currentError > maxError) {
                maxError = currentError;
            }
        }
        //Проверяем, достигнута ли заданная точность вычислений
        if (maxError < eps) {
            return true;
        } else {
            return false;
        }

    }


    private List<List<Number>> makeReducedMatrix(List<List<Number>> matrix) {
        for (int i = 0; i < n; i++) {
            if (matrix.get(i).get(i).doubleValue() == 0.0) {
                //Элемент на главной диагонали равен 0, дальнейшие вычисления сокращенной матрицы невозможны из-за деления на 0
                System.out.println("Элемент на главной диагонали на строке #" + (i + 1) + " равен 0. Составить сокращенную матрицу невозможно.");
                return null;
            }
        }
        List<List<Number>> reducedMatrix = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            reducedMatrix.add(i, new ArrayList<>());
            for (int j = 0; j <= n; j++) {
                if (i != j) {
                    if (j != n) {
                        reducedMatrix.get(i).add(j, matrix.get(i).get(j).doubleValue() / matrix.get(i).get(i).doubleValue());
                    } else {
                        reducedMatrix.get(i).add(j, matrix.get(i).get(j).doubleValue() / matrix.get(i).get(i).doubleValue());
                    }
                } else {
                    reducedMatrix.get(i).add(j, 0);
                }
            }
        }
        return reducedMatrix;
    }

    //Проверка нормы матрицы
    private boolean checkConvergingByNorm(List<List<Number>> matrix) {
        double maxSum = 0;
        for (int i = 0; i < n; i++) {
            double currentSum = 0;
            for (int j = 0; j < n; j++) {
                currentSum += Math.abs(matrix.get(i).get(j).doubleValue());
            }
            if (currentSum > maxSum) {
                maxSum = currentSum;
            }
            if (!(maxSum < 1)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkConvergingByDiagonal(List<List<Number>> matrix) {
        double sum;
        boolean isOK = true;
        System.out.println("Проверка диагонального преобладания:");
        System.out.print("|a_i_i| :");
        matrix.forEach(o -> {
            System.out.print(Math.abs(o.get(matrix.indexOf(o)).doubleValue()) + "  ");
        });
        System.out.print("\n    sum :");
        for (int i = 0; i < n; i++) {
            Number a_i_i = matrix.get(i).get(i).doubleValue();
            sum = matrix.get(i).stream().limit(n).
                    filter(o -> o.doubleValue() != a_i_i.doubleValue()).
                    reduce((number1, number2) -> Math.abs(number1.doubleValue()) + Math.abs(number2.doubleValue())).get().doubleValue();
            isOK = isOK && Math.abs(a_i_i.doubleValue()) >= sum;
            System.out.print(sum + "  ");
        }
        System.out.println();
        return isOK;
    }

    private List<List<Number>> transformMatrix() {
        int[] maxCoefficientIndexes = new int[n];

        //Ищем максимальные коэффициенты в каждой строке и запоминаем их
        for (int i = 0; i < n; i++) {
            double maxCoefficientValue;
            int maxCoefficientIndex = 0;
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                Number currentNumber = extendedMatrix.get(i).get(j);
                sum = extendedMatrix.get(i).stream().limit(n).
                        filter(o -> !o.equals(currentNumber)).reduce((number1, number2) -> Math.abs(number1.doubleValue()) + Math.abs(number2.doubleValue())).get().doubleValue();
                if (Math.abs(currentNumber.doubleValue()) > sum) {
                    maxCoefficientValue = currentNumber.doubleValue();
                    maxCoefficientIndex = j;
                }
            }
            maxCoefficientIndexes[i] = maxCoefficientIndex;
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    //Проверяем, нет ли двух строк, где максимален коэффициент при одном и том же неизвестном
                    if (maxCoefficientIndexes[i] == maxCoefficientIndexes[j]) {
                        //Есть есть две строки, где коэффициенты при одном и том же неизвестном
                        // максимальны, достичь диагонального преобладания перестановкой матрицу не представляется возможным
                        System.out.println("Достичь диагонального преобладания невозможно.");
                        return null;
                    }
                }
            }
        }
        List<List<Number>> transformedMatrix = new ArrayList<>(Collections.nCopies(n, new ArrayList<>(Collections.nCopies(n + 1, 0.0))));
        //Делаем перестановку строк, чтобы на главной диагонали находились наибольшие коэффициенты в каждой строке
        for (int i = 0; i < n; i++) {
            int currentLineIndex = maxCoefficientIndexes[i];
            transformedMatrix.set(currentLineIndex, extendedMatrix.get(i));
        }
        return transformedMatrix;
    }

}





