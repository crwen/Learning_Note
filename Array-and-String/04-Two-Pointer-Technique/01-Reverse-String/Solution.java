import java.util.Arrays;
import java.util.Random;

class Solution {

    private static final String STR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public void reverseString(char[] s) {
        int i = 0;
        int j = s.length - 1;
        while (i < j) {
            swap(s, i, j);
            i++;
            j--;
        }
    }

    public void swap(char[] s, int i, int j) {
        char tmp = s[i];
        s[i] = s[j];
        s[j] = tmp;
    }

    public char[] right(char[] s) {
        char[] res = new char[s.length];
        int len = s.length;
        for (int i = 0;  i < len; i++)
            res[i] = s[len - 1 - i];

        return res;
    }

    public static char[] copyArray(char[] s) {
        char[] res = new char[s.length];
        for (int i = 0; i < s.length; i++)
            res[i] = s[i];
        return res;
    }

    public static boolean isEqual(char[] s1, char[] s2) {
        if (s1.length != s2.length)
            return false;
        for (int i = 0; i < s1.length; i++) {
            if (s1[i] != s2[i]) {
                return false;
            }
        }
        return true;
    }

    public static void print(char[] s) {
        System.out.println(Arrays.toString(s));
    }

    public static void main(String[] args) {
        Solution res = new Solution();
        Random random = new Random();
        int len = STR.length();

        int size = 1000000;
        int t = 1000000;

        for (int i = 0; i < t; i++) {
            char[] arr1 = new char[random.nextInt(size)];
            for (int j = 0; j < arr1.length; j++)
                arr1[j] = STR.charAt(random.nextInt(len));

            char[] arr2 = copyArray(arr1);
            char[] arr3 = copyArray(arr1);

            arr2 = res.right(arr2);
            res.reverseString(arr3);

            if (isEqual(arr2, arr3)) {
                //System.out.println("Nice!!!");
            } else {
                System.out.println("Wrong!!! 　まさか");
                print(arr2);
                print(arr3);
            }
        }

    }


}