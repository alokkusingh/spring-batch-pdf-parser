package com.alok.spring.batch.utils;

public class Utility {
    static public boolean isSalaryTransaction(String transation) {
        if (transation.toLowerCase().matches(".*salary.*|.*evolving.*|.*wipro.*|.*yodlee.*")) {
            if (!transation.toLowerCase().matches(".*reimbursement.*|.*withdrawal.*" +
                    "|.*corp.trf.*|.*trip.*|.*hotel.*|.*ref: .*|.*imps.*")) {
                return true;
            }
        }
        return false;
    }

    static public boolean isFamilyTransaction(String transation) {
        if (transation.toLowerCase().matches(".*ramawatar.*|.*avinash.*|.*gopal.*|.*papa.*|.*31987667084.*|.*3209010000019.*|.*kharagpur.*|.*mb hr.*")) {
            if (!transation.toLowerCase().matches(".*rachna.*|.*withdrawal.*|.*9916661247@.*"
            )) {
                return true;
            }
        }
        return false;
    }

    static public boolean isReversalTransaction(String transation) {
        if (transation.toLowerCase().matches(".*outward  rev.*")) {
            if (!transation.toLowerCase().matches(".*withdrawal.*"
            )) {
                return true;
            }
        }
        return false;
    }
}
