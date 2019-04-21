// Name: Ziyi Gong (zig9)
#define _GNU_SOURCE
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include <sys/types.h>

int input(char* buffer){
	printf("myshell> ");
	fgets(buffer, 300, stdin);
	int len = strlen(buffer) - 1;
	buffer[len] = '\0'; // eliminate \n
	return len;
}

void tokenize(char* str, char** tokens){	
	const char* delim = " \t\n";
	char* token = strtok(str, delim); // 1st token
	int i = 0;
	for (; token != NULL; i++){
		tokens[i] = token;
		token = strtok(NULL, delim);
	}
	tokens[i] = NULL; // terminator
}

void io_redirection(char** tokens){
	unsigned int in = -1; // the index of <
	unsigned int out = -1; // the index of >

	for (int i = 0; tokens[i] != NULL; i++){
		if (strcmp(tokens[i], "<") == 0){
			if (in == -1){ // first occurance
				in = i + 1;
				tokens[i] = NULL; // remove this token

			} else { // illeagal
				fprintf(stderr, "Illeagal syntax of I/O redirection\n");
				exit(1);
			}
		} else if (strcmp(tokens[i], ">") == 0){
			if (out == -1){
				out = i + 1;
				tokens[i] = NULL;

			} else {
				fprintf(stderr, "Illeagal syntax of I/O redirection\n");
				exit(1);
			}
		}
	}
	if (in != -1) freopen(tokens[in], "r", stdin); // < 
	if (out != -1) freopen(tokens[out], "w", stdout); // > 
}

void myexit(const char* status){
	if (status == NULL) exit(0);
	int s = atoi(status);
	if (strlen(status) == 1 && status[0] == 48){ // status = 0
		exit(s);
	} else if (s == 0){ // invalid status, atoi returns 0
		printf("Invalid status identifier: %s\nexit <int: status>\n", status);
		return;
	}

	exit(s);
}

void cd(const char* dir){
	if (dir == NULL) exit(0);
	int status = chdir(dir);
	if (status == -1) printf("Directory %s does not exit!\n", dir);
}

void child_process(char** args){	
	if (fork() == 0){ // child process
		signal(SIGINT, SIG_DFL);
		io_redirection(args);
		execvp(args[0], args);

		perror(args[0]); // error occurs
		exit(1);
	} else { // parent process
		signal(SIGINT, SIG_IGN);
		
		int status;
		int childpid = waitpid(-1, &status, 0);
		
		if (childpid == -1){
			perror(args[0]); // waitpid() returned an error value
		} else if (WIFEXITED(status)){ // exit normally; check exit status
			status = WEXITSTATUS(status);
			if (status != 0) printf("Exited with code %d\n", status);
		} else if (WIFSIGNALED(status)){ // check if signaled
			printf("Terminated due to signal %s\n", 
				strsignal(WTERMSIG(status)));
		} else printf("Terminated abnormally"); // otherwise
	}
}

int main(){
	signal(SIGINT, SIG_IGN); // ignore Ctrl+C
	char** tokens; 
	char buffer[300]; // max 300 characters
	int len; // length of the input string
	while(1){
		len = input(buffer);
		tokens = malloc((len/2 + 1) * sizeof(char*));
		tokenize(buffer, tokens);
		if (tokens[0] == NULL) continue;
		else if (strcmp(tokens[0], "exit") == 0) myexit(tokens[1]);
		else if (strcmp(tokens[0], "cd") == 0) cd(tokens[1]);
		else  child_process(tokens);
		free(tokens);
	}
	
	return 0;
}
