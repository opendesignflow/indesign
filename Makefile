export BRANCH_NAME?=$(shell git branch --show-current)

clean:
	@./gradlew clean
	
publish:
	@./gradlew ooxooGenerate publishToMavenLocal