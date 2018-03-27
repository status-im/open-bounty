FROM python:3.6
COPY end-to-end/requirements.txt /app/requirements.txt
WORKDIR /app
RUN pip3 install -r requirements.txt
COPY end-to-end /app
ENTRYPOINT ["python3"]
CMD ["-m", "pytest", "-m", "sanity"]