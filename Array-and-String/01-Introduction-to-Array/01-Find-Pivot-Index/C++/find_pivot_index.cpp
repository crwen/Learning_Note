#include <iostream>
#include <vector>
using namespace std;

class Solution {
public:
int pivotIndex(vector<int>& nums) {
    int sum = 0;
    vector<int> presum;
    presum.push_back(sum);
    for (int i = 0; i < nums.size(); i++) {
        sum += nums[i];
        presum.push_back(sum);
    }

    int size = presum.size();

    for (int i = 1; i < size; i++) {
        if (presum[i - 1] == presum[size - 1] - presum[i]) {
            return i - 1;
        }
    }
    return -1;
}

};

int main() {
    int arr[] = {-1,-1,0,1,1,0};
    vector<int> vec1(arr, arr + 6);

    cout << Solution().pivotIndex(vec1) << endl;


    return 0;
}
