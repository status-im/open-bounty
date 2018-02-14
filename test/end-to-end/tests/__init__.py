import configparser

class TestData(object):

    def __init__(self):
        self.test_name = None
        self.config = configparser.ConfigParser()

        # define here path to your config.ini file
        # example - config_example.ini

        self.config.read('config.ini')

        # self.issue['title'] is set in GithubPage::create_new_bounty
        # self.issue['id'] is set in GithubPage::create_new_bounty
        # self.local_repo_path is set in GithubPage::clone_repo



test_data = TestData()
