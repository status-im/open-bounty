import configparser

class TestData(object):

    def __init__(self):
        self.test_name = None
        self.config = configparser.ConfigParser()

        # define here path to your config.ini file
        #example - config_example.ini

        self.config.read('config.ini')
        self.base_case_issue = dict()
        self.base_case_issue['title'] = 'Very first auto_test_bounty'


test_data = TestData()
