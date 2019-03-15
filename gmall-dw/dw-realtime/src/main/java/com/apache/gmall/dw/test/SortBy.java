package com.apache.gmall.dw.test;

public class SortBy {
    public static void main(String[] args) {
        int[] arr = {3,2,5,1,6,7,4,8};
        System.out.println("排序前数组为: ");
        for (int i : arr) {
            System.out.print(i + " ");
        }
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j]>arr[j+1]) {
                    /*int temp = arr[j];
                    arr[j]=arr[j+1];
                    arr[j+1]=temp;*/
                    arr[j] = arr[j] + arr[j+1];
                    arr[j+1] = arr[j] - arr[j+1];
                    arr[j] = arr[j] - arr[j+1];
                }
            }
        }
        System.out.println();
        System.out.println("排序后的数组为: ");
        for (int i : arr) {
            System.out.print(i + " ");
        }
    }
}
