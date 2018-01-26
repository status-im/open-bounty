from datetime import datetime


class BasePageObject(object):

    def __init__(self, driver):
        self.driver = driver

    def get_url(self, url):
        self.driver.get(url)

    def refresh(self):
        self.driver.refresh()

    @property
    def time_now(self):
        return datetime.now().strftime('%-m%-d%-H%-M%-S')

    @property
    def current_url(self):
        return self.driver.current_url
