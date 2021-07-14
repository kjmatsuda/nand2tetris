    // push constant 14
    @14
    D=A
    @SP
    A=M     // AレジスタにSPの値を設定(これでMがRAM[256]を指すようになる)
    M=D
    @SP
    M=M+1
    // push constant 7
    @7
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // and
    @SP
    M=M-1
    A=M
    D=M
    @SP
    M=M-1
    A=M
    M=M&D
    @SP
    M=M+1
