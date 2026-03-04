export BRANCH_NAME?=$(shell git branch --show-current)

publish:
	@./gradlew publishToMavenLocal