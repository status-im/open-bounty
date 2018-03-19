from tests import test_data
from datetime import datetime


def pytest_addoption(parser):
    parser.addoption("--build",
                     action="store",
                     default='SOB-' + datetime.now().strftime('%d-%b-%Y-%H-%M'),
                     help="Specify build name")
    parser.addoption('--log',
                     action='store',
                     default=True,
                     help='Display each test step in terminal as plain text: True/False')
    parser.addoption('--env',
                     action='store',
                     default='sauce',
                     help='Specify environment, sauce or local')


def pytest_configure(config):
    if config.getoption('log'):
        import logging
        logging.basicConfig(level=logging.INFO)


def pytest_runtest_setup(item):
    test_data.test_name = item.name
