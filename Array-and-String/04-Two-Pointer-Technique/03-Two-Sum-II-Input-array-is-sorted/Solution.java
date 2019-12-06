
class Solution {
    public int[] twoSum(int[] numbers, int target) {
        int j = numbers.length - 1;
        for (int i = 0; i < j;) {
            int sum = numbers[i] + numbers[j];
            if (sum < target) i++;
            else if (sum > target) j--;
            else return new int[]{i + 1, j + 1};
        }
        return null;
    }
}