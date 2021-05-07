// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.
    @2
    M=0     // R2 を 0 で初期化
(LOOP)
    @1
    D=M     // R1 を D に設定
    @END
    D;JLE
    @1
    M=D-1   // R1 をデクリメント
    @0
    D=M     // R0 を D に設定
    @2
    M=D+M   // R2 に R0 の値を加算
    @LOOP
    0;JMP   // ループ先頭に戻る
(END)
    @END
    0;JMP
