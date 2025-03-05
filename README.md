# To-Do List Application

## Overview

This is a lightweight, single-page to-do list application built with pure Java. The application runs on an embedded HTTP server with no external dependencies or database required.

The project also demonstrates the deployment of a basic Java application using **AWS Elastic Beanstalk**. The application is packaged and deployed through an S3 URL, showcasing the complete deployment workflow from GitHub versioning to the deployment via Elastic Beanstalk.

## Objective

The main objective of this project is to deploy a basic Java application using AWS Elastic Beanstalk. The tasks include:
- Commit application code to a GitHub repository.
- Package the application as a source bundle and upload it to an S3 bucket.
- Deploy the application to Elastic Beanstalk using the S3 URL.
- Optionally, build an automated CI/CD pipeline to package and upload code to S3.

## Features

- **To-Do List:** Users can add, edit, and remove tasks.
- **Java-based:** No external dependencies, running purely on Java.
- **Embedded HTTP server:** The application runs on a lightweight embedded server with no need for a separate database.
- **AWS Elastic Beanstalk Deployment:** The application is deployed using AWS Elastic Beanstalk from an S3 bucket.

## Requirements

- Java 8 or higher
- AWS Account for Elastic Beanstalk and S3
- GitHub for version control

