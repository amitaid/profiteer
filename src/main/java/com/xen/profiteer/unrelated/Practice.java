package com.xen.profiteer.unrelated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Practice {

    public static Set<Set<Integer>> powerset(Set<Integer> set) {
        Set<Set<Integer>> res = new HashSet<>();
        res.add(new HashSet<>());

        for (Integer elem : set) {
            Set<Set<Integer>> tempRes = new HashSet<>();
            for (Set<Integer> s : res) {
                Set<Integer> temp = new HashSet<>(s);
                temp.add(elem);
                tempRes.add(temp);
            }
            res.addAll(tempRes);
        }

        return res;
    }

    public static List<String> perm(String str) {
        List<String> res = new ArrayList<>();
        if (str.isEmpty()) {
            res.add("");
            return res;
        }

        int len = str.length();
        for (int i = 0; i < len; i++) {
            String left = str.substring(0, i) + str.substring(i + 1, len);
            List<String> temp = perm(left);
            for (String s : temp) {
                res.add(str.charAt(i) + s);
            }
        }
        return res;
    }

    public static void main(String[] args) {
        int[] results = new int[6];
        results[0] = 1;
        System.out.println(results[0]);
    }
}
