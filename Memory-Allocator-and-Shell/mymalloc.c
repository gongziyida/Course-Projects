// Ziyi Gong (zig9)

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "mymalloc.h"

// USE THIS GODDAMN MACRO OKAY
#define PTR_ADD_BYTES(ptr, byte_offs) ((void*)(((char*)(ptr)) + (byte_offs)))

// Don't change or remove these constants.
#define MINIMUM_ALLOCATION  16
#define SIZE_MULTIPLE       8

typedef struct Node{
	int size;
	unsigned int used;
	struct Node* next;
	struct Node* prev;
} Node;

Node* head = NULL;
Node* tail = NULL;
Node* last = NULL;

unsigned int round_up_size(unsigned int data_size) {
	if(data_size == 0)
		return 0;
	else if(data_size < MINIMUM_ALLOCATION)
		return MINIMUM_ALLOCATION;
	else
		return (data_size + (SIZE_MULTIPLE - 1)) & ~(SIZE_MULTIPLE - 1);
}

/** find the proper block
 */
Node* find_block(unsigned int data_size){
	Node* current;
	
	for(current = head; current != NULL; current = current-> next)
		if (current-> size >= data_size && ! current-> used) 
			return current;
	
	return NULL;
}

/** append to the linked list
 */
void append(unsigned int size){
	last = sbrk(size + sizeof(Node));
	if (last == NULL) return;
	
	last-> size = size;
	last-> prev = tail;
	
	if (head == NULL) head = last;
	else tail-> next = last;
	
	tail = last;
}

/** split the block found
 */
void split(unsigned int size){
	Node* next = PTR_ADD_BYTES(last, size + sizeof(Node));
	next-> size = last-> size - size - sizeof(Node);
	next-> used = 0;
	next-> next = last-> next;
	next-> prev = last;

	last-> size = size;
	last-> next = next;
}

void* my_malloc(unsigned int size) {
	if(size == 0)
		return NULL;

	size = round_up_size(size);

	// ------- Don't remove anything above this nodene. -------
	
	last = find_block(size);

	if (last == NULL) {
		append(size);
		if (last == NULL) return NULL; // still NULL: no more available space
	}
	else if (last-> size > size + sizeof(Node)) split(size);
	

	last-> used = 1;

	return PTR_ADD_BYTES(last, sizeof(Node));
}

/** coalesce the current free node with its prev or/and next
 *  if they are also free
 */
Node* coalesce(Node* n){
	Node* prev = n-> prev;
	Node* next = n-> next;
	if (prev != NULL){
		if (! prev-> used){
			prev-> size += sizeof(Node) + n-> size;
			prev-> next = n-> next;
			n = prev;
		}
	}
	if (next != NULL){
		if (! next-> used){ 
			n-> size += sizeof(Node) + next-> size;
			n-> next = next-> next;
		}
	}
	// finally, complete the link between this merged block and its next
	if (n-> next != NULL) n-> next-> prev = n;
	
	return n;
}

void my_free(void* ptr) {
	if(ptr == NULL) return;
	Node* to_free = PTR_ADD_BYTES(ptr, -sizeof(Node));
	to_free-> used = 0;

	to_free = coalesce(to_free);

	if (to_free-> next == NULL){
		tail = to_free-> prev;
		if (to_free == head) head = NULL;
		else tail-> next = NULL;
		brk(to_free);
	}
}