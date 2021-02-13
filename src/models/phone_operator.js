
class PhoneOperator {
    static MTN = "MTN Cameroon";
    static ORANGE = "Orange";
    static NEXTTEL = "NEXTTEL";
    static UNKNOWN = "UNKNOWN";
    
    static fromString(phoneOperator = '') {

        if(phoneOperator.startsWith("o") || phoneOperator.startsWith('O')){
            return PhoneOperator.ORANGE;
        }
        if(phoneOperator.startsWith("m") || phoneOperator.startsWith('M')){
            return PhoneOperator.MTN;
        }
        if(phoneOperator.startsWith("n") || phoneOperator.startsWith('n')){
            return PhoneOperator.NEXTTEL;
        }
        return PhoneOperator.UNKNOWN;
    }
}

module.exports = { PhoneOperator }