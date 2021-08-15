    // function SimpleFunction.test 2
(SimpleFunction.test)
    @0
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    @0
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push local 0
    @0
    D=A
    @LCL
    A=D+M
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push local 1
    @1
    D=A
    @LCL
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
    // not
    @SP
    M=M-1
    A=M
    M=!M
    @SP
    M=M+1
    // push argument 0
    @0
    D=A
    @ARG
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
    // push argument 1
    @1
    D=A
    @ARG
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
    //// return
    // FRAME = LCL
    @LCL
    D=M
    @frame
    M=D    
    // RET = *(FRAME - 5)
    @frame
    M=M-5
    A=M    // これで M が *(FRAME - 5) になる
    D=M
    @RET
    M=D    // これで RET に *(FRAME - 5) を設定
    @frame
    M=M+5
    // *ARG = pop()    // pop argument 0 と同じ？
    @0
    D=A
    @ARG
    M=M+D
    @SP
    M=M-1
    A=M
    D=M
    @ARG
    A=M
    M=D
    @0
    D=A
    @ARG
    M=M-D
    // SP = ARG + 1
    @ARG
    D=M+1
    @SP
    M=D
    // THAT = *(FRAME - 1)
    @frame
    M=M-1
    A=M
    D=M
    @THAT
    M=D
    @frame
    M=M+1
    // THIS = *(FRAME - 2)
    @frame
    M=M-2
    A=M
    D=M
    @THIS
    M=D
    @frame
    M=M+2
    // ARG = *(FRAME - 3)
    @frame
    M=M-3
    A=M
    D=M
    @ARG
    M=D
    @frame
    M=M+3
    // LCL = *(FRAME - 4)
    @frame
    M=M-4
    A=M
    D=M
    @LCL
    M=D
    @frame
    M=M+4
    // goto RET
    @RET
    0;JMP
