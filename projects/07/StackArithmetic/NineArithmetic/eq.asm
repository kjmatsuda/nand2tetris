    // push constant 17
    @17
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push constant 17
    @17
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // eq
    @SP
    M=M-1
    A=M
    D=M
    @SP
    M=M-1
    A=M
    D=M-D
    @COND_SATISFIED
    D;JEQ
    @SP
    A=M
    M=0   // false
    @COND_END
    0;JEQ
(COND_SATISFIED)
    @SP
    A=M
    M=-1  // true
(COND_END)
    @SP
    M=M+1
