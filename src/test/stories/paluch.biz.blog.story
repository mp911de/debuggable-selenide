Given I'm on http://www.paluch.biz
When I click Blog
Then the URL ends with /blog.html

Given I'm on http://www.paluch.biz/blog.html
# this one will fail since there are no 99 entries
When I click entry no 99
