name: Feature request
description: Suggest an idea for this project
labels: ["enhancement"]
body:
  - type: checkboxes
    id: searched
    attributes:
      label: Terms
      options:
        - label: "Have you checked the [wiki](https://rockyhawk99.gitbook.io/rockyhawk-wiki/commandpanels/wiki) page to see if your suggestion is already added?
          required: true
        - label: "Have you checked the [Current bugs project](https://github.com/rockyhawk64/CommandPanels/projects/6) to see if your suggestion was already submitted and or denied?"
          required: true
        - label: Have you asked if the feature already exists in the [Discord Server](https://discord.com/invite/eUWBWh7)? (Or got redirected to a Github issue here)
          required: true
  - type: textarea
    id: what-happened
    attributes:
      label: Is your feature request related to a problem? Please describe.
      description: A clear and concise description of what the problem is.
      placeholder: "Example: I'm always frustrated when [...]"
    validations:
      required: true
  - type: solution
    id: steps
    attributes:
      label: Describe the solution you'd like
      description: A clear and concise description of what you want to happen.
    validations:
      required: true
  - type: textarea
    id: alternatives
    attributes:
      label: Describe alternatives you've considered
      description: A clear and concise description of any alternative solutions or features you've considered.
    validations:
      required: true
  - type: textarea
    id: screenshots
    attributes:
      label: Screenshots/Videos (you can drag and drop files or paste links)
      description: Add any other context or screenshots about the feature request here.
  - type: markdown
    attributes:
      value: |
        ## Thanks for taking the time to fill out this feature request!
