        var library = {
          Spark: {
            name: 'Spark App',
            description: 'Spark app with class path',
            icon: 'server',
            inports: [
              {'name': 'in', 'type': 'all'}
            ],
            outports: [
              {'name': 'out', 'type': 'all'}
            ]
          },
          File: {
            name: 'File',
            description: 'File source',
            icon: 'file',
            inports: [
              {'name': 'in', 'type': 'all'},

            ],
            outports: [
              {'name': 'out', 'type': 'all'}
            ]
          },
          MapReduce: {
            name: 'MapReduce',
            description: 'MapReduce app with class path',
            icon: 'file-archive-o',
            inports: [
              {'name': 'in', 'type': 'all'},

            ],
            outports: [
              {'name': 'out', 'type': 'all'}
            ]
          }
        };