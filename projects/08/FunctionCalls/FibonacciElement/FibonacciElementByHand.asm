    //// ブートストラップ
    // SP = 256
    @256
    D=A
    @SP
    M=D
    //// call Sys.init
    @Sys.init
    0;JMP
    //// Sys.vm
    // function Sys.init 0
(Sys.init)
    // push constant 4
    @4
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    //// call Main.fibonacci 1   // computes the 4'th fibonacci element
    // push return-address
    @return-address1
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push LCL
    @LCL
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push ARG
    @ARG
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push THIS
    @THIS
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push THAT
    @THAT
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // ARG = SP - n - 5
    @1
    D=A
    @SP
    M=M-D
    @5
    D=A
    @SP
    M=M-D
    D=M
    @ARG
    M=D
    // LCL = SP
    @SP
    D=A
    @LCL
    M=D
    // goto Main.fibonacci
    @Main.fibonacci
    0;JMP
    // (return-address1)
(return-address1)    
    // label WHILE
(WHILE)    
    // goto WHILE              // loops infinitely
    @WHILE
    0;JMP
    //// Main.vm
    // function Main.fibonacci 0
(Main.fibonacci)    
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
    // push constant 2
    @2
    D=A
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // lt                     // checks if n<2
    @SP
    M=M-1
    A=M
    D=M
    @SP
    M=M-1
    A=M
    D=M-D
    @COND_SATISFIED
    D;JLT
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
    // if-goto IF_TRUE
    @SP
    M=M-1
    A=M
    D=M
    @IF_TRUE
    D;JNE
    // goto IF_FALSE
    @IF_FALSE
    0;JMP
    // label IF_TRUE          // if n<2, return n
(IF_TRUE)
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
    //// return START
    // FRAME = LCL
    @LCL
    D=M
    @frame1
    M=D    
    // RET = *(FRAME - 5)
    @5
    D=A
    @frame1
    M=M-D
    A=M    // これで M が *(FRAME - 5) を指すようになる
    D=M
    @RET
    M=D    // これで RET に *(FRAME - 5) を設定
    @5
    D=A
    @frame1
    M=M+D
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
    @1
    D=A
    @frame1
    M=M-D
    A=M
    D=M
    @THAT
    M=D
    @1
    D=A
    @frame1
    M=M+D
    // THIS = *(FRAME - 2)
    @2
    D=A
    @frame1
    M=M-D
    A=M
    D=M
    @THIS
    M=D
    @2
    D=A
    @frame1
    M=M+D
    // ARG = *(FRAME - 3)
    @3
    D=A
    @frame1
    M=M-D
    A=M
    D=M
    @ARG
    M=D
    @3
    D=A
    @frame1
    M=M+D
    // LCL = *(FRAME - 4)
    @4
    D=A
    @frame1
    M=M-D
    A=M
    D=M
    @LCL
    M=D
    @4
    D=A
    @frame1
    M=M+D
    // goto RET
    @RET
    A=M
    0;JMP
    //// return END
    // label IF_FALSE         // if n>=2, returns fib(n-2)+fib(n-1)
(IF_FALSE)
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
    // push constant 2
    @2
    D=A
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
    //// call Main.fibonacci 1  // computes fib(n-2)
    // push return-address
    @return-address2
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push LCL
    @LCL
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push ARG
    @ARG
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push THIS
    @THIS
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push THAT
    @THAT
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // ARG = SP - n - 5
    @1
    D=A
    @SP
    M=M-D
    @5
    D=A
    @SP
    M=M-D
    D=M
    @ARG
    M=D
    // LCL = SP
    @SP
    D=A
    @LCL
    M=D
    // goto Main.fibonacci
    @Main.fibonacci
    0;JMP
    // (return-address2)
(return-address2)    
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
    // push constant 1
    @1
    D=A
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
    //// call Main.fibonacci 1  // computes fib(n-1)
    // push return-address
    @return-address3
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push LCL
    @LCL
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push ARG
    @ARG
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push THIS
    @THIS
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // push THAT
    @THAT
    D=M
    @SP
    A=M
    M=D
    @SP
    M=M+1
    // ARG = SP - n - 5
    @1
    D=A
    @SP
    M=M-D
    @5
    D=A
    @SP
    M=M-D
    D=M
    @ARG
    M=D
    // LCL = SP
    @SP
    D=A
    @LCL
    M=D
    // goto Main.fibonacci
    @Main.fibonacci
    0;JMP
    // (return-address3)
(return-address3)    
    // add                    // returns fib(n-1) + fib(n-2)
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
    //// return START
    // FRAME = LCL
    @LCL
    D=M
    @frame2
    M=D    
    // RET = *(FRAME - 5)
    @5
    D=A
    @frame2
    M=M-D
    A=M    // これで M が *(FRAME - 5) を指すようになる
    D=M
    @RET
    M=D    // これで RET に *(FRAME - 5) を設定
    @5
    D=A
    @frame2
    M=M+D
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
    @1
    D=A
    @frame2
    M=M-D
    A=M
    D=M
    @THAT
    M=D
    @1
    D=A
    @frame2
    M=M+D
    // THIS = *(FRAME - 2)
    @2
    D=A
    @frame2
    M=M-D
    A=M
    D=M
    @THIS
    M=D
    @2
    D=A
    @frame2
    M=M+D
    // ARG = *(FRAME - 3)
    @3
    D=A
    @frame2
    M=M-D
    A=M
    D=M
    @ARG
    M=D
    @3
    D=A
    @frame2
    M=M+D
    // LCL = *(FRAME - 4)
    @4
    D=A
    @frame2
    M=M-D
    A=M
    D=M
    @LCL
    M=D
    @4
    D=A
    @frame2
    M=M+D
    // goto RET
    @RET
    A=M
    0;JMP
    //// return END
