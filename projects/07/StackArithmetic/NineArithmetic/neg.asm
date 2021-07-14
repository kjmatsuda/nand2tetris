    // push constant 14
    @14
    D=A
    @SP
    A=M     // AレジスタにSPの値を設定(これでMがRAM[256]を指すようになる)
    M=D
    @SP
    M=M+1
    // not
    @SP
    M=M-1
    A=M
    M=-M
    @SP
    M=M+1
