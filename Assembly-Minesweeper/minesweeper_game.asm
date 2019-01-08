.data
	board:			.space	256		# Stores all in the cells
	mineAddresses:	.space	84		# Stores the addresses of mines
	enter:			.space	2		# Stores the one char input by user 
	numCellFlagMine:.space	6		# Stores the numbers of open cells and flags
	msgWaitEnter:	.asciiz "Press Enter key to continue..."
	msgTracking1:   .asciiz "Number of Open Cells: "
	msgTracking2:   .asciiz " Number of flags: "
	msgWin:			.asciiz "You WIN!!!"
	msgLose:		.asciiz "You LOSE!!!"
	
.text
# Global: $t9 is reserved for user's actions; $s7 is reserved for total #rows/columns;
la  $t9, 0x400A0808				# Default: 8*8 board with 10 mines
wait:
	beq  $t9, $zero, wait		# Wait for $t9 to change to nonzero
	andi $t0, $t9, 0xFF000000	# $t0 = the first 8 bits of $t9, user's action
	andi $t1, $t9, 0x00FF0000	# $t1 = the second 8 bits of $t9, #mines
	srl  $t1, $t1, 16			# $t1 = #mines = $t1 >> 16
	andi $t2, $t9, 0x0000FF00	# $t2 = the third 8 bits of $t9, row
	srl  $t2, $t2, 8			# $t2 = row = $t2 >> 8
	andi $t3, $t9, 0x000000FF	# $t3 = the forth 8 bits of $t9, column
	beq  $t0, 0x20000000, reset	# If action ID == 0x20000000, reset
	beq  $t0, 0x40000000, reset # If action ID == 0x40000000, reset
	add  $a0, $zero, $t2		# $a0 = row of the click
	add  $a1, $zero, $t3		# $a1 = col of the click
	add  $a2, $zero, $t0		# $a2 = left or right click
	jal  _click					# call _click
	add  $t9, $zero, $zero		# Set $t9 to 0
	j    wait					# Go back to wait

reset:
	add  $s1, $zero, $t1		# $s1 = $t1 mines in total
	la   $t0, numCellFlagMine	# Load address of numCellFlagMine to $t0
	sh   $s1, 4($t0)			# Store the numbers of mines
	sh   $zero, 0($t0)			# reset numCellFlagMine
	sh   $zero, 2($t0)
	add  $s7, $zero, $t2		# $s7 = $t2 rows / columns in total; *$s7 is reserved for only total # rows/columns
	addi $t0, $zero, 256		# i = 256
	la   $t1, board
resetBoard:
	addi $t0, $t0, -1			# i--
	addi $t2, $zero, 0x09		# $t2 = empty
	sb   $t2, 0($t1)			# Set board[r][c] to empty
	addi $t1, $t1, 1			# Next place in board
	bne  $t0, $zero, resetBoard	# continue if i != 0
	addi $t0, $zero, 20			# i = 20
	la   $t1, mineAddresses
resetMineAddresses:
	addi $t0, $t0, -1			# i--
	sw   $zero, 0($t1)			# Set mineAddresses[i] to zero
	addi $t1, $t1, 4			# Next place in mineAddresses
	bne  $t0, $zero, resetMineAddresses	# continue if i != 0
	la   $s3, mineAddresses		# $s3 = the addresses of mines

mineGenerator:
	la   $s0, board				# Load the address of board
	add  $a0, $s7, $zero 		# Generate radom row index
	jal  _randint
	add  $t0, $v0, $zero
	multu $s7, $t0				# Actual index of the 1st elem of the row: row * #total rows / columns
	mflo $t0					# $t0 = actual index of the 1st elem of the row
	add  $s0, $s0, $t0			# Go to the indicated row
	add  $a0, $s7, $zero		# Generate radom col index
	jal  _randint
	add  $t1, $v0, $zero
	add  $s0, $s0, $t1			# Go to the indicated col
	lb   $t0, 0($s0)			# Get the object at board[r][c]
	bne  $t0, 0x09, mineGenerator	# If board[r][c] != 0, generate a mine again
	addi $t0, $zero, 0x0A		# $t0 is now a mine
	sb   $t0, 0($s0)			# Load the mine
	sw   $s0, 0($s3)			# Save the address of this mine to mineAddresses
	addi $s3, $s3, 4			# Next word of mineAddresses
	addi $s1, $s1, -1			# $s1 - 1 mines remaining to generate and load onto the board
	bne  $s1, $zero, mineGenerator	# If there are still some mines to load, continue
	
la   $s0, mineAddresses			# $s0 = mineAddresses
la   $s1, board					# $s1 = board
numGenerator:
	lw   $s5, 0($s0)			# $s5 = address
	addi $s0, $s0, 4			# Next address
	beq  $s5, $zero, done		# if address == null: break
	sub  $t1, $s5, $s1			# $t1 = relative address of this mine
	divu $t1, $s7				# Divide the relative address of this mine by $s7, #rows as reserved above
	mflo $s3					# $s3 = row of this mine
	mfhi $s4					# $s4 = col of this mine
	add  $a0, $zero, $s3
	add  $a1, $zero, $s5
	jal  _upperAndLowerNum		# upperAndLowerNum($a0=row, $a1=address)
	beq  $s4, $zero, rightCol	# If col == 0, there is no need to process the left column; go to rightCol
	add  $a0, $zero, $s3		# To generate the three numbers in the left column beside the mine	
	addi $a1, $s5, -1
	jal  _upperAndLowerNum		# upperAndLowerNum($a0=row, $a1=address-1)
	addi $t1, $s7, -1
	beq  $s4, $t1, numGenerator	# If col == 9, there is no need to process the right column; continue
rightCol:						# To generate the three numbers in the right column beside the mine
	add  $a0, $zero, $s3
	addi $a1, $s5, 1
	jal  _upperAndLowerNum		# upperAndLowerNum($a0=row, $a1=address+1)
	j    numGenerator
	
done:
	jal  _displayAndCloseAll	# Copy everything from board to 0xffff8000
	add  $t9, $zero, $zero
	j    wait
	
# To generate psudorandom int
# Argument:
#	-$a0: the upper limit (exclusive) of random int
# Return:
#	-$v0: the random int
_randint:
	add  $t0, $a0, $zero		# Copy the upper limit
	add  $a0, $zero, $zero		# Generate random int in [0,$a0)
	add  $a1, $t0, $zero
	addi $v0, $zero, 42
	syscall
	add  $v0, $a0, $zero
	jr   $ra
	
# To copy everything from board array to 0xffff8000
# Argument: none
# Return: none
_displayAndCloseAll:
	addi $sp, $sp, -4
	sw   $ra, 0($sp)
	la   $t0, board				# Load the address of board
	li   $t1, 0xffff8000		# Load the address of display
	addi $t2, $zero, 256		# Counter i
__copyLoop:
	addi $t2, $t2, -1			# i--
	lb   $t3, 0($t0)			# Load what is in board[r][c]
	sb   $t3, 0($t1)			# Store it to array 0xffff8000
	addi $t0, $t0, 1			# Next board elem
	addi $t1, $t1, 1			# Next cell
	bne  $t2, $zero, __copyLoop	# continue copying if i != 0
	la   $a0, msgWaitEnter		# prompt the user to press enter
	addi $v0, $zero, 4
	syscall
__waitEnter:
	la   $a0, enter
	addi $a1, $zero, 1
	addi $v0, $zero, 8
	syscall
	lb   $t0, 0($a0)			# Read the char
	bne  $t0, 0, __waitEnter	# If it's \n, break
	li   $t0, 0xffff8000		# Load displaying address
	addi $t1, $zero, 256		# Counter j = 256
__closeLoop:
	sb	 $zero, 0($t0)			# Close each cell
	addi $t0, $t0, 1			# Address ++
	addi $t1, $t1, -1			# j--
	bne  $t1, $zero, __closeLoop # If j=0, break
	jal  _tracking
	lw   $ra, 0($sp)
	addi $sp, $sp, 4
	jr   $ra


# To generate the numbers in the upper and lower rows adjacent to the origin
# Arguments:
#	-$a0: the row of the origin
#	-$a1: the actual address of the origin
#Return: none
_upperAndLowerNum:
	addi $sp, $sp, -8
	sw   $s0, 4($sp)
	sw   $ra, 0($sp)
	add  $s0, $a0, $zero		# Copy $a0
	add  $a0, $a1, $zero		# Pass $a1 to $a0 as argument
	jal  _modifyCell
	beq  $s0, $zero, __lower	# If the origin is at row 0, meaning there's no need to process the upper one, go to __lower
	sub  $a0, $a1, $s7			# Pass $a1-$s7 to $a0 as argument
	jal  _modifyCell
	addi $t0, $s7, -1
	beq  $s0, $t0, __doneUpperAndLowerNum # If the row of the origin is the last row, done
__lower:
	add  $a0, $a1, $s7  		# Pass $a1+$s7 to $a0 as argument
	jal  _modifyCell
__doneUpperAndLowerNum:
	lw   $s0, 4($sp)
	lw   $ra, 0($sp)
	addi $sp, $sp, 8
	jr   $ra
	
# Helper method for _upperAndLowerNum modifying the cell
# Arguments:
#	-$a0: the actual address of the cell
# Return: none
_modifyCell:
	addi $sp, $sp, -4
	sw   $ra, 0($sp)
	lb   $t0, 0($a0)			# Load what is at the cell
	beq  $t0, 0x0A, __doneModification	# If the origin is a mine, done
	beq  $t0, 0x09, __make1		# If the origin is a blank, go to __make1
	addi $t0, $t0, 1			# Else, simply add one
	j    __doneModification
__make1:
	addi $t0, $zero, 1
__doneModification:
	sb   $t0, 0($a0)			# Store $t0 to $a0
	lw   $ra, 0($sp)
	addi $sp, $sp, 4
	jr   $ra
	
# To print the proper message indicating a clicking activity
# Arguments:
#	-$a0: row
#	-$a1: column
#	-$a2: left or right click
# Return: none
_click:
	addi $sp, $sp, -8
	sw   $ra, 0($sp)
	sw   $s0, 4($sp)
	add  $t0, $a0, $zero		# Copy $a0 to $t0
	add  $t1, $a1, $zero		# Copy $a1 to $t1
	multu $t0, $s7				# Actual row index = row * # total rows = $t0 * $s7
	mflo $t0					# $t0 = relative "row" index in 1 dimension
	add  $s0, $t0, $t1			# $t0 = relative address in 1 dimension
	la   $t2, board				# Load board address to $t2
	add  $t2, $t2, $s0			# $t2 = actual board address
	la   $t3, 0xffff8000		# Load display address to $t3
	add  $t3, $t3, $s0			# $t3 = actual display address
	beq  $a2, 0x88000000, __rc  # Right click
	lb   $t0, 0($t3)			# $t0 = what is on the cell clicked
	beq  $t0, 0x09, __doneClick # If the user clicked on an open empty cell, return
	slti $t1, $t0, 0x0A			# $t0 = 1 if $t0 < 0x0A
	bne  $t1, 1, __doneClick	# If the displayed is mine, crossed mine, flag, or exploded mine, return
	slt  $t1, $zero, $t0		# $t1 = 1 if 0 < $t0
	beq  $t1, 1, __onNum		# If the user clicked on an open num
	lb   $t0, 0($t2)			# $t0 = what is in the cell clicked
	beq  $t0, 0x0A, __mines
	beq  $t0, 0x09, __empty		# If beneath empty
	sb   $t0, 0($t3)			# If not mine: display whatever in the cell
	la   $t0, numCellFlagMine		# Load address of numCellFlagMine to $t0
	lh   $t1, 0($t0)
	addi $t1, $t1, 1			# numbers of open cells + 1
	sh   $t1, 0($t0)			# Restore numbers of open cells
	jal  _tracking
	j    __doneClick
__onNum:
	lb   $t0, 0($t3)			# $t0 = num displayed
	addi $t2, $s7, -1			# $t2 = max row/col
	beq  $a1, $zero, __rFlag  	# Ignore the whole left side if it is at col 0
	lb   $t1, -1($t3)
	seq  $t1, $t1, 0x0C			# $t1 = 1 if there is a flag straight left
	sub  $t0, $t0, $t1			# $t0 - $t1 = #flags remaining
__rFlag:
	beq  $a1, $t2, __uFlag		# Ignore the whole left side if it is at max col
	lb   $t1, 1($t3)
	seq  $t1, $t1, 0x0C			# $t1 = 1 if there is a flag straight right
	sub  $t0, $t0, $t1			# $t0 - $t1 = #flags remaining
__uFlag:
	beq  $a0, $zero, __loFlag	# Ignore the upper side if it is at row 0
	sub  $t4, $t3, $s7
	lb   $t1, 0($t4)
	seq  $t1, $t1, 0x0C			# $t1 = 1 if there is a flag straight up
	sub  $t0, $t0, $t1
	beq  $a1, $zero, __ruFlag	# Ignore the upper left side if it is at col 0
	lb   $t1, -1($t4)
	seq  $t1, $t1, 0x0C			# $t1 = 1 if there is a flag on the left upper side
	sub  $t0, $t0, $t1
__ruFlag:
	lb   $t1, 1($t4)
	seq  $t1, $t1, 0x0C
	sub  $t0, $t0, $t1			# $t1 = 1 if there is a flag on the right upper side
__loFlag:
	beq  $a0, $t2, __countDone 	# Ignore the lower side if it is at max row
	add  $t4, $t3, $s7
	lb   $t1, 0($t4)
	seq  $t1, $t1, 0x0C			# $t1 = 1 if there is a flag straight down
	sub  $t0, $t0, $t1
	beq  $a1, $zero, __rlFlag	# Ignore the lower left side if it is at col 0
	lb   $t1, -1($t4)
	seq  $t1, $t1, 0x0C			# $t1 = 1 if there is a flag on the left lower side
	sub  $t0, $t0, $t1
__rlFlag:
	lb   $t1, 1($t4)
	seq  $t1, $t1, 0x0C			# $t1 = 1 if there is a flag on the right lower side
	sub  $t0, $t0, $t1
__countDone:
	bne  $t0, $zero, __doneClick # If $t0 != 0, return
	sb   $zero, 0($t3)			# For convenience, first make the cell closed
	la   $t0, numCellFlagMine		# Load address of numCellFlagMine to $t0
	lh   $t1, 0($t0)
	addi $t1, $t1, -1			# numbers of open cells - 1, since previously one cell was closed
	sh   $t1, 0($t0)			# Restore numbers of open cells
	add  $a2, $zero, $s0		 # $a0 = row, $a1 = col
	jal  _recOpen
	jal  _tracking
	j    __doneClick
__empty:
	add  $a2, $zero, $s0		# $a0 = row, $a1 = col
	jal  _recOpen
	jal  _tracking
	j    __doneClick
__mines:
	addi $t0, $zero, 0x0D		# Exploded mine in $t0
	sb   $t0, 0($t3)			# Display exploded mine
	la   $t0, mineAddresses
	la   $t1, board
	li   $t2, 0xffff8000
__mineDisLoop:
	lw   $t3, 0($t0)			# Load the address of a mine
	beq  $t3, $zero, __lose		# If there's no more mine address, display "lose" and return
	addi $t0, $t0, 4			# Next address
	sub  $t3, $t3, $t1			# $t3 = relative address
	add  $t3, $t3, $t2			# $t3 = display address
	lb   $t4, 0($t3)			# get what's on the cell
	beq  $t4, $zero, __mine		# Just display the mine
	beq  $t4, 0x0C, __xMine		# display the crossed mine
	j    __mineDisLoop
__lose:
	la   $a0, msgLose
	addi $v0, $zero, 4
	syscall						# Print "You LOSE"
	j    __doneClick
__mine:
	addi $t4, $zero, 0x0A		# Mine in $t4
	sb   $t4, 0($t3)			# Mine displayed
	j    __mineDisLoop
__xMine:
	addi $t4, $zero, 0x0B		# Mine in $t4
	sb   $t4, 0($t3)			# Mine displayed
	j    __mineDisLoop
__rc:
	lb   $t0, 0($t3)			# $t0 = what is on the cell clicked
	beq  $t0, $zero, __addFlag	# Add flag
	beq  $t0, 0x0C, __rmFlag	# remove flag
	j    __doneClick
__addFlag:
	addi $t0, $zero, 0x0C		# Flag in $t0
	sb   $t0, 0($t3)
	la   $t0, numCellFlagMine		# Load address of numCellFlagMine to $t0
	lh   $t1, 2($t0)
	addi $t1, $t1, 1			# numbers of flags + 1
	sh   $t1, 2($t0)			# Restore the numbers of flags
	jal  _tracking
	j    __doneClick
__rmFlag:
	add  $t0, $zero, $zero		# Empty in $t0
	sb   $t0, 0($t3)
	la   $t0, numCellFlagMine		# Load address of numCellFlagMine to $t0
	lh   $t1, 2($t0)
	addi $t1, $t1, -1			# numbers of flags - 1
	sh   $t1, 2($t0)			# Restore the numbers of flags
	jal  _tracking
__doneClick:
	lw   $ra, 0($sp)
	lw   $s0, 4($sp)
	addi $sp, $sp, 8
	jr   $ra
	
# To display a single cell
# Arguments:
#	-$a0: the row of the cell
#   -$a1: the col of the cell
#   -$a2: the memory index of the cell
# Return: none
_recOpen:
	addi $sp, $sp, -24
	sw   $s0, 0($sp)
	sw   $s1, 4($sp)
	sw   $s2, 8($sp)
	sw   $s3, 12($sp)
	sw   $s4, 16($sp)
	sw   $ra, 20($sp)
	addi $s3, $s7, -1			# $s3 = max row/col index
	la   $t0, 0xffff8000		# Load display address in $t0
	add  $t0, $t0, $a2			# $t0 = cell's display address
	lb   $t1, 0($t0)			# get what's displayed on the cell
	bne  $t1, 0x00, __doneWithoutOpen # If it has already been open, return
	la   $t1, board				# Load display address in $t1
	add  $t1, $t1, $a2			# $t1 = cell's board address
	lb   $t1, 0($t1)			# Get what's in the cell
	beq  $t1, 0x0A, __doneWithoutOpen # If there is a mine beneath, return
	sb   $t1, 0($t0)			# Display it
	slti $t1, $t1, 0x09			# $t1 = 1 if $t1 < 0x09
	bne  $t1, 1, __notNum		# If the displayed is a number, check if it's the first number (what the user clicked)
	la   $t1, __empty			# Load the largest possible address to which the method will return
	slt  $t1, $t1, $ra			# $t1 = 1 if $t1 < $ra, i.e. the AR before this one is also from _recOpen
	beq  $t1, 1, __doneRecOpen	# If this one is called by _recOpen (itself), return
__notNum:
	add  $s0, $a0, $zero		# Backup
	add  $s1, $a1, $zero
	add  $s2, $a2, $zero
	add  $s4, $zero, $zero			# $s4 to record if the cell is on any bounds
	beq  $s1, $zero, __afterLCheck	# If at the left bound: do not recOpen left
	add  $a0, $s0, $zero			# Row unchanged
	addi $a1, $s1, -1				# Col - 1
	addi $a2, $s2, -1
	jal  _recOpen					# _recOpen(row, col, memoryIndex)
	addi $s4, $s4, 1				# bit 0 := true of not on left bound
__afterLCheck:
	beq  $s1, $s3, __afterRCheck	# If at the right bound: do not recOpen right
	add  $a0, $s0, $zero			# Row unchanged
	addi $a1, $s1, 1				# Col + 1
	addi $a2, $s2, 1
	jal  _recOpen					# _recOpen(row, col, memoryIndex)
	addi $s4, $s4, 2				# bit 1 := true of not on right bound
__afterRCheck:	
	beq  $s0, $zero, __afterUCheck	# If at the upper bound: do not recOpen up
	addi $a0, $s0, -1				# Row - 1
	add  $a1, $s1, $zero			# Col unchanged
	sub  $a2, $s2, $s7
	jal  _recOpen					# _recOpen(row, col, memoryIndex)
	addi $s4, $s4, 4				# bit 2 := true of not on upper bound
__afterUCheck:
	beq  $s0, $s3, __afterLoCheck	# If at the lower bound: do not recOpen lower
	addi $a0, $s0, 1				# Row + 1
	add  $a1, $s1, $zero			# Col unchanged
	add  $a2, $s2, $s7
	jal  _recOpen					# _recOpen(row, col, memoryIndex)
	addi $s4, $s4, 8				# bit 3 := true of not on lower bound
__afterLoCheck:
	andi $t0, $s4, 5				# $t0 = bit 2 and bit 0
	bne  $t0, 5, __afterLUCheck		# If at the left and/ or upper bound: do not recOpen l-u
	addi $a0, $s0, -1				# Row - 1
	addi $a1, $s1, -1				# Col - 1
	addi $a2, $s2, -1
	sub  $a2, $a2, $s7
	jal  _recOpen					# _recOpen(row, col, memoryIndex)
__afterLUCheck:
	andi $t0, $s4, 6				# $t0 = bit 2 and bit 1
	bne  $t0, 6, __afterRUCheck		# If at the right and/ or upper bound: do not recOpen r-u
	addi $a0, $s0, -1				# Row - 1
	addi $a1, $s1, 1				# Col + 1
	addi $a2, $s2, 1
	sub  $a2, $a2, $s7
	jal  _recOpen					# _recOpen(row, col, memoryIndex)
__afterRUCheck:	
	andi $t0, $s4, 9				# $t0 = bit 3 and bit 0
	bne  $t0, 9, __afterLLCheck		# If at the left and/ or lower bound: do not recOpen l-l
	addi $a0, $s0, 1				# Row + 1
	addi $a1, $s1, -1				# Col - 1
	addi $a2, $s2, -1
	add  $a2, $a2, $s7
	jal  _recOpen					# _recOpen(row, col, memoryIndex)
__afterLLCheck:
	andi $t0, $s4, 10				# $t0 = bit 2 and bit 0
	bne  $t0, 10, __doneRecOpen		# If at the right and/ or lower bound: do not recOpen r-l
	addi $a0, $s0, 1				# Row + 1
	addi $a1, $s1, 1				# Col + 1
	addi $a2, $s2, 1
	add  $a2, $a2, $s7
	jal  _recOpen					# _recOpen(row, col, memoryIndex)
__doneRecOpen:
	la   $t0, numCellFlagMine	# Load address of numCellFlagMine to $t0
	lh   $t1, 0($t0)
	addi $t1, $t1, 1			# numbers of open cells + 1
	sh   $t1, 0($t0)			# Restore the numbers of open cells
__doneWithoutOpen:
	lw   $s0, 0($sp)
	lw   $s1, 4($sp)
	lw   $s2, 8($sp)
	lw   $s3, 12($sp)
	lw   $s4, 16($sp)
	lw   $ra, 20($sp)
	addi $sp, $sp, 24
	jr   $ra
	
# To display tracking msg
# Arguments: none
# Return: none
_tracking:
	la   $t0, numCellFlagMine
	la   $a0, msgTracking1
	addi $v0, $zero, 4
	syscall						# Print "Num open Cells: "
	lh   $a0, 0($t0)			# $a0 = #Open cells
	addi $v0, $zero, 1
	syscall						# Print #Open cells
	la   $a0, msgTracking2
	addi $v0, $zero, 4
	syscall						# Print "Num flags: "
	lh   $a0, 2($t0)			# $a0 = #FLags
	addi $v0, $zero, 1
	syscall						# Print #Flags
	addi $a0, $zero, 10			# $a0 = \n
	addi $v0, $zero, 11
	syscall						# Print \n
	lh   $t1, 2($t0)			# $t1 = #Flags
	lh   $t0, 4($t0)			# $t0 = #mines
	bne  $t0, $t1, __doneTracking
	la   $t0, mineAddresses
	la   $t1, board
	li   $t2, 0xffff8000
__checkWinLoop:
	lw   $t3, 0($t0)			# get a mine address
	beq  $t3, $zero, __win		# If end of array: win
	sub  $t3, $t3, $t1			# get the mine's relative address
	add  $t3, $t3, $t2			# get the display cell's address
	lb   $t3, 0($t3)			# get what is displayed
	bne  $t3, 0x0C, __doneTracking # if the user mislabeled the mine, return
	addi $t0, $t0, 4			# Next mine address
	j    __checkWinLoop
__win:
	la   $a0, msgWin
	addi $v0, $zero, 4
	syscall						# Print "You Win"
__doneTracking:
	jr   $ra