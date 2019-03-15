package com.apache.gmall.dw.test;

public class QuickSort {


    public static void quickS(int[] numbers, int start, int end) {
        if (start < end) {
            int base = numbers[start]; // 选定的基准值（第一个数值作为基准值）
            int temp; // 记录临时中间值
            int i = start, j = end;
            do {
                while ((numbers[i] < base) && (i < end))
                    i++;
                while ((numbers[j] > base) && (j > start))
                    j--;
                if (i <= j) {
                    temp = numbers[i];
                    numbers[i] = numbers[j];
                    numbers[j] = temp;
                    i++;
                    j--;
                }
            } while (i <= j);
            if (start < j)
                quickS(numbers, start, j);
            if (end > i)
                quickS(numbers, i, end);
        }
    }

    public static void main(String[] args) {

        int[] numbers = {9, -16, 21, 23, -30, -49, 21, 30, 30};
        quickS(numbers, 0, numbers.length-1);
        for (int i : numbers) {
            System.out.print(i + " ");
        }
    }
}
