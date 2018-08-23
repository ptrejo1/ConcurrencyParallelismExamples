Compiler-generated assembly code from the simd.c program
	
main:
   ## %rsp is the 'stack pointer', which points to the TOP position on the stack.
	
   ## An expression such as 96(%rsp) is an address or pointer expression: %rsp is the base address,
      96 is of the offset therefrom. The offset is in bytes, so in this case it's 96 bytes.
      (The Intel stack grows downwards, from high to low addresses.)

   ## The full expression 'movl $1, 96(%rsp)' copies (not literally moves) the 32-bit value 1 into stack
      location %rsp + 96:

	The stack
	  ...                       ## high addresses
	+-----+
	|  1  |<------%rsp + 96     ## instruction 'movl $l, 96(%rsp)' puts 1 in this location
        +-----+
	  ...                
	+-----+
	|     |
	+-----+
	|     |<------%rsp   ## %rsp is the 64-bit 'stack pointer'
	+-----+
	  ...                       ## low addresses
	
   ## Note that every offset increases by 4 because the values (1,2,3,...) are 4-byte (32-bit) integer values.
   movl $1, 96(%rsp)         # data vector 1 start
   movl $2, 100(%rsp)
   movl $3, 104(%rsp)
   movl $4, 108(%rsp)
   movl $5, 112(%rsp)
   movl $6, 116(%rsp)
   movl $7, 120(%rsp)
   movl $8, 124(%rsp)        # data vector 1 end
   movl $8, 128(%rsp)        # data vector 2 start
   movl $7, 132(%rsp)
   movl $6, 136(%rsp)
   movl $5, 140(%rsp)
   movl $4, 144(%rsp)
   movl $3, 148(%rsp)
   movl $2, 152(%rsp)
   movl $1, 156(%rsp)        # data vector 2 end
	
   movdqa 96(%rsp), %xmm1    # %xmm1 = dataV1[0..3]: 'dq' is for 'double quadword'
	                     # (quad == 64 bits) and %xmm1 is a 128-bit register
	
   movdqa 128(%rsp), %xmm0   # %xmm0 = dataV2[0..3]
	
   paddd %xmm0, %xmm1        # %xmm1 += %xmm0 (the 'paddd' instruction is for 'packed addition' of 32-bit values)
	
   movdqa 112(%rsp), %xmm2   # %xmm2 = dataV1[4..7]
   movdqa 144(%rsp), %xmm0   # %xmm0 = dataV2[4..7]
   paddd %xmm2, %xmm0        # %xmm0 += %xmm2
	
   movdqa %xmm1, 32(%rsp)    # save %xmm1 to stack (sums of the 1st four elements)
   movdqa %xmm0, 48(%rsp)    # save %xmm0 to stack (sums of the last four elements)
	                     # the sum is now on the stack contiguously, in effect as an array
   ...
