package com.alok.spring.batch.utils;

public class Utility {
    static public boolean isSalaryTransaction(String transation) {
        if (transation.toLowerCase().matches(".*salary.*|.*evolving.*|.*wipro.*|.*yodlee.*|.*bosch.*")) {
            if (!transation.toLowerCase().matches(".*reimbursement.*|.*withdrawal.*" +
                    "|.*corp.trf.*|.*trip.*|.*hotel.*|.*ref: .*|.*imps.*")) {
                return true;
            }
        }
        return false;
    }

    static public boolean isFamilyTransaction(String transation) {
        if (transation.toLowerCase().matches(".*ramawatar.*|.*avinash.*|.*gopal.*|.*papa.*|.*31987667084.*" +
                "|.*3209010000019.*|.*kharagpur.*|.*mb hr.*|.*kumari  jyoti.*|.*pankaj  kumar.*|.*bihar.*" +
                "|.*yogendra  narayan.*|.*ashok  thakral.*|.*shailendra  singh.*|.*manju  devi.*|.*vivekanand  singh.*")) {
            if (!transation.toLowerCase().matches(".*rachna.*|.*withdrawal.*|.*9916661247@.*"
            )) {
                return true;
            }
        }
        return false;
    }

    static public boolean isReversalTransaction(String transation) {
        if (transation.toLowerCase().matches(".*outward  rev.*") || transation.toLowerCase().matches(".*rev:imps.*")) {
            if (!transation.toLowerCase().matches(".*withdrawal.*"
            )) {
                return true;
            }
        }
        return false;
    }
}
