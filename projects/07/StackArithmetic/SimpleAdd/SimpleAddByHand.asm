    // push constant 7
    @7
    D=A     // Dレジスタに7を設定
    @SP     // AレジスタにSPを設定
    A=M     // AレジスタにSPの値を設定(これでMがRAM[256]を指すようになる)
    M=D
    @SP
    M=M+1
    // push constant 8
    @8
    D=A     // Dレジスタに8を設定
    @SP     // AレジスタにSPを設定
    A=M     // AレジスタにSPの値を設定(これでMがRAM[257]を指すようになる)
    M=D
    @SP
    M=M+1
    // add 
    @SP
    M=M-1
    A=M
    D=M     // Dレジスタに8を設定
    @SP
    M=M-1
    A=M
    M=D+M
    @SP
    M=M+1
