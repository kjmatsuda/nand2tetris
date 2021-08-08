    // push constant 3030
    @3030
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // pop pointer 0
    @SP
    M=M-1
    A=M
    D=M
    @R3
    M=D
    // push constant 3040
    @3040
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // pop pointer 1
    @SP
    M=M-1
    A=M
    D=M
    @R4
    M=D
    // push constant 32
    @32
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // pop this 2
    @2
    D=A
    @THIS
    M=M+D
    @SP
    M=M-1
    A=M
    D=M
    @THIS
    A=M
    M=D
    @2
    D=A
    @THIS
    M=M-D
    // push constant 46
    @46
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // pop that 6
    @6
    D=A
    @THAT
    M=M+D
    @SP
    M=M-1
    A=M
    D=M
    @THAT
    A=M
    M=D
    @6
    D=A
    @THAT
    M=M-D
    // push pointer 0
    @R3
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push pointer 1
    @R4
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // add
    @SP
    M=M-1
    A=M
    D=M
    @SP
    M=M-1
    A=M
    M=D+M
    @SP
    M=M+1
    // push this 2
    @2
    D=A
    @THIS
    A=D+M
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // sub
    @SP
    M=M-1
    A=M
    D=M
    @SP
    M=M-1
    A=M
    M=M-D
    @SP
    M=M+1
    // push that 6
    @6
    D=A
    @THAT
    A=D+M
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // add
    @SP
    M=M-1
    A=M
    D=M
    @SP
    M=M-1
    A=M
    M=D+M
    @SP
    M=M+1
